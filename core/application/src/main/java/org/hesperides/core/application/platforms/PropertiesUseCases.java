package org.hesperides.core.application.platforms;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.application.platforms.properties.PropertyType;
import org.hesperides.core.application.platforms.properties.PropertyValuationBuilder;
import org.hesperides.core.application.platforms.properties.PropertyValuationContext;
import org.hesperides.core.domain.events.queries.EventQueries;
import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.exceptions.ForbiddenOperationException;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.queries.ModulePropertiesView;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.platforms.PlatformPropertiesUpdatedEvent;
import org.hesperides.core.domain.platforms.commands.PlatformCommands;
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff.ComparisonMode;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitorsSequence;
import org.hesperides.core.domain.platforms.exceptions.DeployedModuleNotFoundException;
import org.hesperides.core.domain.platforms.exceptions.PlatformNotFoundException;
import org.hesperides.core.domain.platforms.queries.PlatformQueries;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.platforms.queries.views.PropertiesEventView;
import org.hesperides.core.domain.platforms.queries.views.properties.*;
import org.hesperides.core.domain.platforms.queries.views.properties.PlatformDetailedPropertiesView.DetailedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.PlatformDetailedPropertiesView.ModuleDetailedPropertyView;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.hesperides.core.application.platforms.PlatformUseCases.isRestrictedPlatform;
import static org.hesperides.core.application.platforms.properties.PropertyType.GLOBAL;
import static org.hesperides.core.application.platforms.properties.PropertyType.WITHOUT_MODEL;
import static org.hesperides.core.application.platforms.properties.PropertyValuationBuilder.buildPropertyVisitorsSequenceForGlobals;
import static org.hesperides.core.domain.platforms.entities.DeployedModule.extractModulePathFromPropertiesPath;
import static org.hesperides.core.domain.platforms.entities.Platform.GLOBAL_PROPERTIES_PATH;
import static org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty.containsDuplicateKeys;
import static org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView.excludeUnusedValues;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
@Slf4j
public class PropertiesUseCases {

    private final PlatformCommands platformCommands;
    private final PlatformQueries platformQueries;
    private final PlatformUseCases platformUseCases;
    private final ModuleQueries moduleQueries;
    private final EventQueries eventQueries;
    private final PropertyReferenceScanner propertyReferenceScanner;

    @Autowired
    public PropertiesUseCases(PlatformCommands platformCommands,
                              PlatformQueries platformQueries,
                              PlatformUseCases platformUseCases, ModuleQueries moduleQueries,
                              EventQueries eventQueries,
                              PropertyReferenceScanner propertyReferenceScanner) {
        this.platformCommands = platformCommands;
        this.platformQueries = platformQueries;
        this.platformUseCases = platformUseCases;
        this.moduleQueries = moduleQueries;
        this.eventQueries = eventQueries;
        this.propertyReferenceScanner = propertyReferenceScanner;
    }

    public static Long getPropertiesVersionId(PlatformView platform, String propertiesPath) {
        Long propertiesVersionId;

        if (Platform.isGlobalPropertiesPath(propertiesPath)) {
            propertiesVersionId = platform.getGlobalPropertiesVersionId();
        } else {
            propertiesVersionId = platform.findActiveDeployedModules()
                    .filter(deployedModule -> deployedModule.getPropertiesPath().equals(propertiesPath))
                    .findFirst()
                    .orElseThrow(() -> new DeployedModuleNotFoundException(platform.getPlatformKey(), propertiesPath))
                    .getPropertiesVersionId();
        }
        return Optional.ofNullable(propertiesVersionId)
                .orElse(DeployedModule.INIT_PROPERTIES_VERSION_ID);
    }

    private static PropertyVisitorsSequence buildModulePropertyVisitorsSequence(PlatformView platform,
                                                                                String modulePath,
                                                                                Module.Key moduleKey,
                                                                                List<AbstractPropertyView> modulePropertiesModels,
                                                                                String instanceName,
                                                                                boolean shouldHidePasswordProperties) {

        EnumSet<PropertyType> propertiesToInclude = EnumSet.of(GLOBAL, WITHOUT_MODEL);
        DeployedModuleView deployedModule = platform.findActiveDeployedModuleByModulePathAndModuleKey(modulePath, moduleKey);

        PropertyVisitorsSequence firstPropertyVisitorsSequence = PropertyValuationBuilder.buildFirstPropertyVisitorsSequence(
                deployedModule, modulePropertiesModels, shouldHidePasswordProperties, propertiesToInclude);

        PropertyValuationContext valuationContext = PropertyValuationBuilder.buildValuationContext(
                firstPropertyVisitorsSequence, deployedModule, platform, instanceName);

        PropertyVisitorsSequence finalPropertyVisitorsSequence = PropertyValuationBuilder.buildFinalPropertyVisitorsSequence(
                valuationContext, firstPropertyVisitorsSequence, propertiesToInclude);

        // Le diff a besoin des propriétés prédéfinies et globales pour comparer les valeurs *finales*
        // des propriétés au niveau du module, en revanche on ne veut pas les inclure dans la comparaison
        finalPropertyVisitorsSequence = valuationContext.removePredefinedProperties(finalPropertyVisitorsSequence);
        return valuationContext.removeGlobalPropertiesThatAreNotInTheModel(finalPropertyVisitorsSequence, modulePropertiesModels);
    }

    public Long getPropertiesVersionId(Platform.Key platformKey, String propertiesPath, Long timestamp) {
        PlatformView platform = getPlatform(platformKey, timestamp);
        return getPropertiesVersionId(platform, propertiesPath);
    }

    public PropertiesDiff getPropertiesDiff(Platform.Key fromPlatformKey,
                                            String fromPropertiesPath,
                                            String fromInstanceName,
                                            Platform.Key toPlatformKey,
                                            String toPropertiesPath,
                                            String toInstanceName,
                                            @Nullable Long timestamp,
                                            @Nullable Long originTimestamp,
                                            ComparisonMode comparisonMode,
                                            User user) {

        PlatformView fromPlatform = getPlatform(fromPlatformKey, originTimestamp);
        PlatformView toPlatform = getPlatform(toPlatformKey, timestamp);

        PropertiesDiff propertiesDiff;
        if (Platform.isGlobalPropertiesPath(fromPropertiesPath) && Platform.isGlobalPropertiesPath(toPropertiesPath)) {
            PropertyVisitorsSequence fromPropertyVisitors = buildPropertyVisitorsSequenceForGlobals(fromPlatform);
            PropertyVisitorsSequence toPropertyVisitors = buildPropertyVisitorsSequenceForGlobals(toPlatform);
            propertiesDiff = new PropertiesDiff(fromPropertyVisitors, toPropertyVisitors, comparisonMode);

        } else {

            if (Platform.isGlobalPropertiesPath(fromPropertiesPath) || Platform.isGlobalPropertiesPath(toPropertiesPath)) {
                throw new IllegalArgumentException("You can't compare global properties with module or instance properties");
            }

            Module.Key fromModuleKey = Module.Key.fromPropertiesPath(fromPropertiesPath);
            String fromModulePath = extractModulePathFromPropertiesPath(fromPropertiesPath);
            Module.Key toModuleKey = Module.Key.fromPropertiesPath(toPropertiesPath);
            String toModulePath = extractModulePathFromPropertiesPath(toPropertiesPath);

            // Note: on devrait passer le timestamp aux 2 appels ci-dessous, cf. issue #724
            List<AbstractPropertyView> fromModulePropertiesModels = moduleQueries.getPropertiesModel(fromModuleKey);
            List<AbstractPropertyView> toModulePropertiesModels = moduleQueries.getPropertiesModel(toModuleKey);

            boolean fromShouldHidePasswordProperties = isRestrictedPlatform(user, fromPlatform);
            boolean toShouldHidePasswordProperties = isRestrictedPlatform(user, toPlatform);

            PropertyVisitorsSequence fromPropertyVisitors = buildModulePropertyVisitorsSequence(
                    fromPlatform, fromModulePath, fromModuleKey,
                    fromModulePropertiesModels,
                    fromInstanceName, fromShouldHidePasswordProperties);
            PropertyVisitorsSequence toPropertyVisitors;
            try {
                toPropertyVisitors = buildModulePropertyVisitorsSequence(
                        toPlatform, toModulePath, toModuleKey,
                        toModulePropertiesModels,
                        toInstanceName, toShouldHidePasswordProperties);
            } catch (ModuleNotFoundException error) {
                if (timestamp != null) { // We make the error message more explicit:
                    error = new ModuleNotFoundException(toModuleKey, toModulePath, timestamp);
                }
                throw error;
            }

            propertiesDiff = new PropertiesDiff(fromPropertyVisitors, toPropertyVisitors, comparisonMode);
        }
        return propertiesDiff;
    }

    public List<AbstractValuedPropertyView> getValuedProperties(Platform.Key platformKey, String propertiesPath, Long timestamp, User user) {
        List<AbstractValuedPropertyView> properties = new ArrayList<>();

        PlatformView platform = getPlatform(platformKey, timestamp);

        if (Platform.isGlobalPropertiesPath(propertiesPath)) {
            properties.addAll(platform.getGlobalProperties());
        } else if (StringUtils.isNotEmpty(propertiesPath)) {
            Module.Key moduleKey = Module.Key.fromPropertiesPath(propertiesPath);
            // Note: on devrait passer le timestamp aux 2 appels ci-dessous, cf. issue #724
            List<AbstractPropertyView> modulePropertiesModel = moduleQueries.getPropertiesModel(moduleKey);

            properties = platform.findActiveDeployedModuleByPropertiesPath(propertiesPath).getValuedProperties();

            // On exclue les propriétés non valorisées ayant une valeur par défaut
            properties = AbstractValuedPropertyView.excludePropertiesWithOnlyDefaultValue(properties, modulePropertiesModel);

            // Pas besoin de récupérer la plateforme entière juste pour ce test
            // surtout si c'est pour refaire une requête pour récupérer les propriétés
            // => Créer une requête isProductionPlatform ou réutiliser la plateforme
            // pour récupérer les propriétés
            if (isRestrictedPlatform(user, platform)) {
                properties = AbstractValuedPropertyView.hidePasswordProperties(properties, modulePropertiesModel);
            }
        }
        return properties;
    }

    public Map<String, Set<GlobalPropertyUsageView>> getGlobalPropertiesUsage(Platform.Key platformKey) {
        PlatformView platform = platformQueries.getOptionalPlatform(platformKey).orElseThrow(() -> new PlatformNotFoundException(platformKey));

        // On ne tient compte que des modules utilisés dans la platforme (pas des modules sauvegardés)
        List<DeployedModuleView> deployedModules = platform.findActiveDeployedModules().collect(toList());
        List<TemplateContainer.Key> moduleKeys = platform.getActiveDeployedModulesKeys();
        List<ModulePropertiesView> modulesProperties = moduleQueries.getModulesProperties(moduleKeys);

        return platform.getGlobalProperties().stream()
                .map(ValuedPropertyView::getName)
                .collect(toMap(identity(), globalPropertyName ->
                        GlobalPropertyUsageView.getGlobalPropertyUsage(globalPropertyName, deployedModules, modulesProperties)));
    }

    public List<String> getInstancesModel(Platform.Key platformKey, String propertiesPath) {
        if (!platformQueries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }
        return platformQueries.getInstancesModel(platformKey, propertiesPath);
    }

    public List<AbstractValuedPropertyView> saveProperties(Platform.Key platformKey,
                                                           String propertiesPath,
                                                           Long platformVersionId,
                                                           List<AbstractValuedProperty> abstractValuedProperties,
                                                           Long propertiesVersionId,
                                                           String userComment,
                                                           User user) {
        PlatformView platform = platformUseCases.getPlatform(platformKey);
        if (isRestrictedPlatform(user, platform)) {
            throw new ForbiddenOperationException("Setting properties of a production platform is reserved to production role");
        }

        if (containsDuplicateKeys(abstractValuedProperties)) {
            throw new IllegalArgumentException("Saving properties with duplicate keys is forbidden");
        }

        Long expectedPropertiesVersionId = getPropertiesVersionId(platform, propertiesPath);

        if (Platform.isGlobalPropertiesPath(propertiesPath)) {
            List<ValuedProperty> valuedProperties = AbstractValuedProperty.filterAbstractValuedPropertyWithType(abstractValuedProperties, ValuedProperty.class);
            // Platform properties are global and should always be of type ValuedProperty
            if (valuedProperties.size() != abstractValuedProperties.size()) {
                throw new IllegalArgumentException("Global properties should always be valued properties");
            }
            platformCommands.savePlatformProperties(
                    platform.getId(),
                    platformVersionId,
                    propertiesVersionId,
                    expectedPropertiesVersionId,
                    valuedProperties,
                    defaultString(userComment, EMPTY),
                    user);
        } else {
            Module.Key moduleKey = Module.Key.fromPropertiesPath(propertiesPath);
            if (!moduleQueries.moduleExists(moduleKey)) {
                throw new ModuleNotFoundException(moduleKey);
            }
            validateRequiredAndPatternProperties(abstractValuedProperties, moduleKey, platformKey);
            platformCommands.saveModulePropertiesInPlatform(
                    platform.getId(),
                    propertiesPath,
                    platformVersionId,
                    propertiesVersionId,
                    expectedPropertiesVersionId,
                    abstractValuedProperties,
                    defaultString(userComment, EMPTY),
                    user);
        }

        return getValuedProperties(platformKey, propertiesPath, null, user);
    }

    /**
     * Vérifie que les propriétés obligatoires sont bien valorisées
     * et que celles ayant un pattern le respecte bien.
     */
    private void validateRequiredAndPatternProperties(List<AbstractValuedProperty> abstractValuedProperties, Module.Key moduleKey, Platform.Key platformKey) {
        // On récupère d'abord toutes les propriétés du module et les propriétés valorisées.
        // Cela inclut les propriétés définies ou valorisées à l'intérieur des propriétés itérables.
        List<ValuedProperty> allValuedProperties = AbstractValuedProperty.getFlatValuedProperties(abstractValuedProperties);
        // On doit tenir compte des propriétés globales
        List<ValuedPropertyView> globalProperties = platformQueries.getGlobalProperties(platformKey);
        allValuedProperties.addAll(ValuedPropertyView.toDomainValuedProperties(globalProperties));

        List<AbstractPropertyView> moduleProperties = moduleQueries.getPropertiesModel(moduleKey);
        Stream<PropertyView> allSimpleProperties = AbstractPropertyView.getAllSimpleProperties(moduleProperties);
        allSimpleProperties.forEach(moduleProperty -> moduleProperty.validateRequiredAndPatternProperties(allValuedProperties));
    }

    public void purgeUnusedProperties(Platform.Key platformKey, String propertiesPath, User user) {
        PlatformView platform = platformUseCases.getPlatform(platformKey);
        if (isRestrictedPlatform(user, platform)) {
            throw new ForbiddenOperationException("Cleaning properties of a production platform is reserved to production role");
        }
        if (Platform.isGlobalPropertiesPath(propertiesPath)) {
            throw new IllegalArgumentException("Cleaning only works for module properties, not for global properties");
        }

        DeployedModuleView deployedModule = platform.findActiveDeployedModuleByPropertiesPath(propertiesPath);
        Module.Key moduleKey = Module.Key.fromPropertiesPath(propertiesPath);

        List<AbstractPropertyView> propertiesModel = moduleQueries.getPropertiesModel(moduleKey);
        List<AbstractValuedPropertyView> baseValues = deployedModule.getValuedProperties();
        Set<String> referencedProperties = propertyReferenceScanner.findAll(baseValues, deployedModule.getInstances());

        List<AbstractValuedProperty> filteredValuedProperties = excludeUnusedValues(baseValues, propertiesModel, referencedProperties)
                .map(AbstractValuedPropertyView::toDomainValuedProperty)
                .map(AbstractValuedProperty.class::cast)
                .collect(toList());

        Long propertiesVersionId = getPropertiesVersionId(platform, propertiesPath);

        platformCommands.saveModulePropertiesInPlatform(platform.getId(), propertiesPath, platform.getVersionId(),
                propertiesVersionId, propertiesVersionId, filteredValuedProperties,
                "Generated comment: cleaning unused properties", user);
    }

    public PlatformDetailedPropertiesView getDetailedProperties(Platform.Key platformKey, String propertiesPath, User user) {
        PlatformView platform = platformUseCases.getPlatform(platformKey);
        boolean hidePasswords = isRestrictedPlatform(user, platform);
        List<TemplateContainer.Key> moduleKeys = platform.getActiveDeployedModulesKeys(propertiesPath);
        Map<Module.Key, List<AbstractPropertyView>> propertiesByModuleKey = moduleQueries.getModulesProperties(moduleKeys).stream()
                .collect(toMap(ModulePropertiesView::getModuleKey, ModulePropertiesView::getProperties));

        // Propriétés globales détaillées
        PropertyVisitorsSequence globalPropertyVisitorsSequence = buildPropertyVisitorsSequenceForGlobals(platform);
        List<DetailedPropertyView> globalProperties = globalPropertyVisitorsSequence.toGlobalDetailedProperties();

        // Propriétés détaillées de chaque module de la plateforme
        List<ModuleDetailedPropertyView> modulesDetailedProperties = platform.findActiveDeployedModules()
                .filter(deployedModule -> StringUtils.isEmpty(propertiesPath) || deployedModule.getPropertiesPath().equals(propertiesPath))
                .flatMap(deployedModule -> {
                    List<AbstractPropertyView> propertiesModel = propertiesByModuleKey.get(deployedModule.getModuleKey());
                    PropertyVisitorsSequence propertyVisitorsSequence = buildModulePropertyVisitorsSequence(
                            platform,
                            deployedModule.getModulePath(),
                            deployedModule.getModuleKey(),
                            propertiesModel,
                            null, hidePasswords);

                    // Calcul des propriétés inutilisées
                    List<AbstractValuedPropertyView> valuedProperties = deployedModule.getValuedProperties();
                    Set<String> referencedProperties = propertyReferenceScanner.findAll(valuedProperties, deployedModule.getInstances());
                    List<AbstractValuedPropertyView> filteredValuedProperties = excludeUnusedValues(valuedProperties, propertiesModel, referencedProperties)
                            .collect(toList());
                    Set<String> unusedProperties = valuedProperties.stream()
                            .filter(valuedProperty -> !filteredValuedProperties.contains(valuedProperty))
                            .map(AbstractValuedPropertyView::getName)
                            .collect(Collectors.toSet());

                    return propertyVisitorsSequence.toModuleDetailedProperties(
                            deployedModule.getPropertiesPath(), globalProperties, unusedProperties);
                })
                .collect(toList());

        return new PlatformDetailedPropertiesView(
                platform.getApplicationName(),
                platform.getPlatformName(),
                globalProperties,
                modulesDetailedProperties);
    }

    public List<PropertiesEventView> getPropertiesEvents(User user,
                                                         Platform.Key platformKey,
                                                         String propertiesPath,
                                                         Integer page,
                                                         Integer size) {

        String platformId = platformUseCases.findPlatformId(platformKey);
        boolean isModuleProperties = !GLOBAL_PROPERTIES_PATH.equals(propertiesPath);
        boolean hidePasswords = shouldHidePropertiesEventsPasswords(isModuleProperties, platformId, user, platformKey.getApplicationName());

        List<EventView> events = isModuleProperties
                ? eventQueries.getLastToFirstPlatformModulePropertiesUpdatedEvents(platformId, propertiesPath, page, size)
                : eventQueries.getLastToFirstEventsByType(platformId, PlatformPropertiesUpdatedEvent.class, page, size);

        List<PropertiesEventView> propertiesEvents = new ArrayList<>();
        if (!isEmpty(events)) {
            // On récupère le premier évènement de la page suivante pour faire
            // la comparaison avec le dernier élément de la liste en cours
            Optional<EventView> firstEventOfNextPage = (isModuleProperties
                    ? eventQueries.getLastToFirstPlatformModulePropertiesUpdatedEvents(platformId, propertiesPath, page + 1, size)
                    : eventQueries.getLastToFirstEventsByType(platformId, PlatformPropertiesUpdatedEvent.class, page + 1, size)
            ).stream().findFirst();
            // On en profite pour déterminer si le premier évènement
            // en date fait partie de la liste en cours
            boolean shouldExtractCreationEvent = true;
            if (firstEventOfNextPage.isPresent()) {
                events.add(firstEventOfNextPage.get());
                shouldExtractCreationEvent = false;
            }

            Set<String> passwordPropertyNames = hidePasswords ? extractPasswordProperties(propertiesPath) : emptySet();
            propertiesEvents = PropertiesEventView.buildPropertiesEvents(events, isModuleProperties, shouldExtractCreationEvent).stream()
                    .map(propertiesEvent -> hidePasswords ? propertiesEvent.hidePasswords(passwordPropertyNames) : propertiesEvent)
                    .sorted(comparing(PropertiesEventView::getTimestamp).reversed())
                    .collect(toList());
        }
        return propertiesEvents;
    }

    private boolean shouldHidePropertiesEventsPasswords(boolean isModuleProperties, String platformId, User user, String applicationName) {
        boolean hidePasswords = false;
        if (isModuleProperties) {
            boolean isProductionPlatform = platformQueries.isProductionPlatform(platformId);
            hidePasswords = isRestrictedPlatform(isProductionPlatform, user, applicationName);
        }
        return hidePasswords;
    }

    private Set<String> extractPasswordProperties(String propertiesPath) {
        Module.Key moduleKey = Module.Key.fromPropertiesPath(propertiesPath);
        List<AbstractPropertyView> moduleProperties = moduleQueries.getPropertiesModel(moduleKey);
        return AbstractPropertyView.getAllSimpleProperties(moduleProperties)
                .filter(PropertyView::isPassword)
                .map(PropertyView::getName)
                .collect(toUnmodifiableSet());
    }

    private PlatformView getPlatform(Platform.Key platformKey, Long timestamp) {
        return timestamp != null ? platformUseCases.getPlatformAtPointInTime(platformKey, timestamp) : platformUseCases.getPlatform(platformKey);
    }
}
