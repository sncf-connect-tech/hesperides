package org.hesperides.core.application.platforms;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.application.platforms.properties.PropertyType;
import org.hesperides.core.application.platforms.properties.PropertyValuationBuilder;
import org.hesperides.core.application.platforms.properties.PropertyValuationContext;
import org.hesperides.core.domain.events.commands.EventCommands;
import org.hesperides.core.domain.events.queries.EventQueries;
import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.exceptions.ForbiddenOperationException;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.queries.ModulePropertiesView;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.platforms.PlatformCreatedEvent;
import org.hesperides.core.domain.platforms.PlatformPropertiesUpdatedEvent;
import org.hesperides.core.domain.platforms.PlatformUpdatedEvent;
import org.hesperides.core.domain.platforms.commands.PlatformCommands;
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff.ComparisonMode;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitorsSequence;
import org.hesperides.core.domain.platforms.exceptions.ApplicationNotFoundException;
import org.hesperides.core.domain.platforms.exceptions.DeployedModuleNotFoundException;
import org.hesperides.core.domain.platforms.exceptions.DuplicatePlatformException;
import org.hesperides.core.domain.platforms.exceptions.PlatformNotFoundException;
import org.hesperides.core.domain.platforms.queries.PlatformQueries;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.platforms.queries.views.properties.*;
import org.hesperides.core.domain.platforms.queries.views.properties.PlatformDetailedPropertiesView.DetailedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.PlatformDetailedPropertiesView.ModuleDetailedPropertyView;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.security.queries.ApplicationDirectoryGroupsQueries;
import org.hesperides.core.domain.security.queries.views.ApplicationDirectoryGroupsView;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.technos.queries.TechnoQueries;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateContainerKeyView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.hesperides.core.application.platforms.properties.PropertyType.GLOBAL;
import static org.hesperides.core.application.platforms.properties.PropertyType.WITHOUT_MODEL;
import static org.hesperides.core.application.platforms.properties.PropertyValuationBuilder.buildPropertyVisitorsSequenceForGlobals;
import static org.hesperides.core.domain.platforms.entities.Platform.GLOBAL_PROPERTIES_PATH;
import static org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView.excludeUnusedValues;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
@Slf4j
public class PlatformUseCases {

    @Value("${hesperides.events-query-size-factor}")
    private Integer eventsQuerySizeFactor;

    private final PlatformCommands platformCommands;
    private final PlatformQueries platformQueries;
    private final ModuleQueries moduleQueries;
    private final TechnoQueries technoQueries;
    private final ApplicationDirectoryGroupsQueries applicationDirectoryGroupsQueries;
    private final EventCommands eventCommands;
    private final EventQueries eventQueries;
    private final PropertyReferenceScanner propertyReferenceScanner;

    @Autowired
    public PlatformUseCases(PlatformCommands platformCommands,
                            PlatformQueries platformQueries,
                            ModuleQueries moduleQueries,
                            TechnoQueries technoQueries,
                            ApplicationDirectoryGroupsQueries applicationDirectoryGroupsQueries,
                            EventCommands eventCommands,
                            EventQueries eventQueries,
                            PropertyReferenceScanner propertyReferenceScanner) {
        this.platformCommands = platformCommands;
        this.platformQueries = platformQueries;
        this.moduleQueries = moduleQueries;
        this.technoQueries = technoQueries;
        this.applicationDirectoryGroupsQueries = applicationDirectoryGroupsQueries;
        this.eventCommands = eventCommands;
        this.eventQueries = eventQueries;
        this.propertyReferenceScanner = propertyReferenceScanner;
    }

    public String createPlatform(Platform platform, User user) {
        if (isRestrictedPlatform(user, platform)) {
            throw new ForbiddenOperationException("Creating a production platform is reserved to production role");
        }
        if (platformQueries.platformExists(platform.getKey())) {
            throw new DuplicatePlatformException(platform.getKey());
        }
        return cleanCreatePlatform(platform, user);
    }

    public String copyPlatform(Platform newPlatform, Platform.Key existingPlatformKey, boolean copyInstancesAndProperties, User user) {
        if (platformQueries.platformExists(newPlatform.getKey())) {
            throw new DuplicatePlatformException(newPlatform.getKey());
        }
        PlatformView existingPlatform = platformQueries.getOptionalPlatform(existingPlatformKey)
                .orElseThrow(() -> new PlatformNotFoundException(existingPlatformKey));
        if ((newPlatform.isProductionPlatform() || existingPlatform.isProductionPlatform()) && !user.hasProductionRoleForApplication(newPlatform.getKey().getApplicationName())) {
            throw new ForbiddenOperationException("Creating a platform from a production platform is reserved to production role");
        }
        List<DeployedModule> deployedModules = DeployedModuleView.toDomainDeployedModules(existingPlatform.findActiveDeployedModules());
        if (!copyInstancesAndProperties) {
            deployedModules = deployedModules.stream()
                    .map(DeployedModule::copyWithoutInstancesNorProperties)
                    .collect(toList());
        }
        List<ValuedProperty> globalProperties = copyInstancesAndProperties ? ValuedPropertyView.toDomainValuedProperties(existingPlatform.getGlobalProperties()) : emptyList();
        // cf. createPlatformFromExistingPlatform in https://github.com/voyages-sncf-technologies/hesperides/blob/fix/3.0.3/src/main/java/com/vsct/dt/hesperides/applications/AbstractApplicationsAggregate.java#L156
        Platform newFullPlatform = new Platform(
                newPlatform.getKey(),
                newPlatform.getVersion(),
                newPlatform.isProductionPlatform(),
                1L,
                deployedModules,
                DeployedModule.INIT_PROPERTIES_VERSION_ID,
                globalProperties
        );
        return cleanCreatePlatform(newFullPlatform, user);
    }

    public PlatformView getPlatform(String platformId) {
        return platformQueries.getOptionalPlatform(platformId)
                .orElseThrow(() -> new PlatformNotFoundException(platformId));
    }

    public PlatformView getPlatform(Platform.Key platformKey) {
        return getPlatform(platformKey, false);
    }

    public PlatformView getPlatform(Platform.Key platformKey, boolean withPasswordFlag) {
        PlatformView platform = platformQueries.getOptionalPlatform(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
        if (withPasswordFlag) {
            boolean hasPasswords = !isEmpty(getPlatformsWithPassword(Collections.singletonList(platform)));
            platform = platform.withPasswordIndicator(hasPasswords);
        }
        return platform;
    }

    public PlatformView getPlatformAtPointInTime(Platform.Key platformKey, long timestamp) {
        String platformId = findPlatformId(platformKey);
        return platformQueries.getPlatformAtPointInTime(platformId, timestamp);
    }

    public void updatePlatform(Platform.Key platformKey, Platform newPlatform, boolean copyPropertiesForUpgradedModules, User user) {
        PlatformView existingPlatform = platformQueries.getOptionalPlatform(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
        List<DeployedModule> existingDeployedModule = DeployedModuleView.toDomainDeployedModules(existingPlatform.getDeployedModules().stream());
        newPlatform = newPlatform.retrieveExistingOrInitializePropertiesVersionIds(existingDeployedModule);

        if (!user.hasProductionRoleForApplication(platformKey.getApplicationName())) {
            if (existingPlatform.isProductionPlatform() && newPlatform.isProductionPlatform()) {
                throw new ForbiddenOperationException("Updating a production platform is reserved to production role");
            }
            if (existingPlatform.isProductionPlatform() || newPlatform.isProductionPlatform()) {
                throw new ForbiddenOperationException("Upgrading a platform to production is reserved to production role");
            }
        }
        platformCommands.updatePlatform(existingPlatform.getId(), newPlatform, copyPropertiesForUpgradedModules, user);
    }

    public void deletePlatform(Platform.Key platformKey, User user) {
        PlatformView platform = platformQueries.getOptionalPlatform(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
        if (isRestrictedPlatform(user, platform)) {
            throw new ForbiddenOperationException("Deleting a production platform is reserved to production role");
        }
        platformCommands.deletePlatform(platform.getId(), platformKey, user);
    }

    public ApplicationView getApplication(String applicationName, boolean hidePlatformsModules, boolean withPasswordFlag) {
        ApplicationView applicationView = platformQueries.getApplication(applicationName, hidePlatformsModules)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationName));

        Optional<ApplicationDirectoryGroupsView> applicationDirectoryGroups = applicationDirectoryGroupsQueries.getApplicationDirectoryGroups(applicationName);
        if (applicationDirectoryGroups.isPresent()) {
            applicationView = applicationView.withDirectoryGoups(applicationDirectoryGroups.get());
        }

        if (withPasswordFlag) {
            Set<Platform.Key> platformsWithPassword = getPlatformsWithPassword(applicationView.getPlatforms());
            applicationView = applicationView.withPasswordIndicator(platformsWithPassword);
        }

        return applicationView;
    }

    private Set<Platform.Key> getPlatformsWithPassword(List<PlatformView> platforms) {

        List<Module.Key> allPlatformsModuleKeys = platforms.stream()
                .map(PlatformView::getDeployedModules)
                .flatMap(List::stream)
                .map(DeployedModuleView::getModuleKey)
                .distinct()
                .collect(toList());

        List<ModuleView> allPlatformsModules = moduleQueries.getModulesWithin(allPlatformsModuleKeys);
        List<Module.Key> modulesWithPassword = moduleQueries.getModulesWithPasswordWithin(allPlatformsModuleKeys);

        List<Techno.Key> allModulesTechnoKeys = allPlatformsModules.stream()
                // On exclut les modules dont on sait déjà qu'ils contiennent au moins un mot de passe
                .filter(module -> !modulesWithPassword.contains(module.getKey()))
                .map(ModuleView::getTechnos)
                .flatMap(List::stream)
                .map(TechnoView::getKey)
                .map(TemplateContainerKeyView::toTechnoKey)
                .distinct()
                .collect(toList());

        Map<Module.Key, List<Techno.Key>> technoKeysByModuleMap = allPlatformsModules.stream().collect(toMap(
                ModuleView::getKey,
                module -> module.getTechnos().stream()
                        .map(TechnoView::getKey)
                        .map(TemplateContainerKeyView::toTechnoKey)
                        .distinct()
                        .collect(toList())
        ));

        List<Techno.Key> technosWithPassword = technoQueries.getTechnosWithPasswordWithin(allModulesTechnoKeys);

        // Récupère la liste des modules ayant au moins une techno contenant un mot de passe
        // et la concatène avec la liste de modules contenant au moins un mot de passe
        Set<Module.Key> allModulesWithPassword = Stream.concat(modulesWithPassword.stream(),
                technoKeysByModuleMap.entrySet().stream()
                        .filter(entry -> entry.getValue().stream().anyMatch(technosWithPassword::contains))
                        .map(Map.Entry::getKey))
                .collect(Collectors.toSet());

        Map<Platform.Key, List<Module.Key>> moduleKeysByPlatformMap = platforms.stream().collect(toMap(
                PlatformView::getPlatformKey,
                platform -> platform.getDeployedModules().stream()
                        .map(DeployedModuleView::getModuleKey)
                        .distinct()
                        .collect(toList())
        ));

        return moduleKeysByPlatformMap.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(allModulesWithPassword::contains))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public List<ModulePlatformView> getPlatformsUsingModule(Module.Key moduleKey) {
        return platformQueries.getPlatformsUsingModule(moduleKey);
    }

    public List<SearchApplicationResultView> getApplicationNames() {
        return platformQueries.getApplicationNames();
    }

    public List<SearchApplicationResultView> searchApplications(String applicationName) {
        List<SearchApplicationResultView> applications;
        if (isBlank(applicationName)) {
            applications = platformQueries.getApplicationNames();
        } else {
            applications = platformQueries.searchApplications(applicationName);
        }
        return applications;
    }

    public List<SearchPlatformResultView> searchPlatforms(String applicationName, String platformName) {
        return platformQueries.searchPlatforms(applicationName, platformName);
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

    public Long getPropertiesVersionId(Platform.Key platformKey, String propertiesPath, Long timestamp) {
        PlatformView platform = timestamp != null ? getPlatformAtPointInTime(platformKey, timestamp) : getPlatform(platformKey);
        return getPropertiesVersionId(platform, propertiesPath);
    }

    public PropertiesDiff getPropertiesDiff(Platform.Key fromPlatformKey,
                                            String fromPropertiesPath,
                                            String fromInstanceName,
                                            Platform.Key toPlatformKey,
                                            String toPropertiesPath,
                                            String toInstanceName,
                                            @Nullable Long timestamp,
                                            ComparisonMode comparisonMode,
                                            User user) {

        PlatformView fromPlatform = getPlatform(fromPlatformKey);
        PlatformView toPlatform = timestamp != null ? getPlatformAtPointInTime(toPlatformKey, timestamp) : getPlatform(toPlatformKey);

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

    private static String extractModulePathFromPropertiesPath(String propertiesPath) {
        String[] parts = propertiesPath.split("#");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Too short properties path: " + propertiesPath);
        }
        parts = Arrays.copyOfRange(parts, 0, parts.length - 3);
        return String.join("#", parts);
    }

    public List<AbstractValuedPropertyView> getValuedProperties(Platform.Key platformKey, String propertiesPath, Long timestamp, User user) {
        List<AbstractValuedPropertyView> properties = new ArrayList<>();

        PlatformView platform = timestamp != null ? getPlatformAtPointInTime(platformKey, timestamp) : getPlatform(platformKey);

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
        PlatformView platform = getPlatform(platformKey);
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
            platformCommands.savePlatformProperties(platform.getId(), platformVersionId, propertiesVersionId, expectedPropertiesVersionId, valuedProperties, user);
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
                    StringUtils.defaultString(userComment, StringUtils.EMPTY),
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

    public PlatformView restoreDeletedPlatform(Platform.Key platformKey, User user) {
        if (platformQueries.platformExists(platformKey)) {
            throw new IllegalArgumentException("Cannot restore an existing platform");
        }
        String platformId = platformQueries.getOptionalPlatformIdFromEvents(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
        platformCommands.restoreDeletedPlatform(platformId, user);
        return platformQueries.getOptionalPlatform(platformId).get();
    }

    public List<ApplicationView> getAllApplicationsDetail(boolean withPasswordFlag) {
        List<ApplicationView> applications = platformQueries.getAllApplicationsDetail();

        if (withPasswordFlag) {
            List<PlatformView> allApplicationsPlatforms = applications.stream()
                    .map(ApplicationView::getPlatforms)
                    .flatMap(List::stream)
                    .distinct()
                    .collect(toList());

            Set<Platform.Key> platformsWithPassword = getPlatformsWithPassword(allApplicationsPlatforms);
            applications = applications.stream()
                    .map(application -> application.withPasswordIndicator(platformsWithPassword))
                    .collect(toList());
        }

        return applications;
    }

    public void purgeUnusedProperties(Platform.Key platformKey, String propertiesPath, User user) {
        PlatformView platform = getPlatform(platformKey);
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
        PlatformView platform = getPlatform(platformKey);
        boolean hidePasswords = isRestrictedPlatform(user, platform);
        List<TemplateContainer.Key> moduleKeys = platform.getActiveDeployedModulesKeys(propertiesPath);
        Map<Module.Key, List<AbstractPropertyView>> propertiesByModuleKey = moduleQueries.getModulesProperties(moduleKeys).stream()
                .collect(toMap(ModulePropertiesView::getModuleKey, ModulePropertiesView::getProperties));

        // Propriétés globales détaillées
        PropertyVisitorsSequence globalPropertyVisitorsSequence = buildPropertyVisitorsSequenceForGlobals(platform);
        List<DetailedPropertyView> globalProperties = globalPropertyVisitorsSequence.toGlobalDetailedProperties();

        // Propriétés détaillées de chaque module de la plateforme
        List<ModuleDetailedPropertyView> modulesDetailedProperties = platform.findActiveDeployedModules()
                .filter(deployedModule -> isEmpty(propertiesPath) || deployedModule.getPropertiesPath().equals(propertiesPath))
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

    private static boolean containsDuplicateKeys(List<AbstractValuedProperty> list) {
        return list.stream()
                .map(AbstractValuedProperty::getName)
                .map(StringUtils::trim)
                .collect(Collectors.toSet()).size() != list.size();
    }

    private String cleanCreatePlatform(Platform platform, User user) {
        eventCommands.cleanAggregateEvents(platform.getKey().generateHash());
        return platformCommands.createPlatform(platform, user);
    }

    public List<PlatformEventView> getPlatformEvents(Platform.Key platformKey, Integer clientPage, Integer size) {

        // Certains évènements de mise à jour de plateforme ne contiennent
        // que l'incrémentation du version_id. On ne peut donc pas appliquer
        // la pagination sur la requête à l'EventStore mais une fois qu'on a
        // la liste des vraies modifications de la plateforme.

        // Mais comme dans certains cas le nombre d'évènements est si élevé
        // que les performances se dégradent significativement.

        // Pour palier à ça, on récupère un nombre d'évènements supérieur à
        // ce qui est demandé en retour (eventsQuerySizeFactor) et si ce
        // n'est pas suffisant, on récupère les évènements suivants.

        // La pagination finale est appliquée à la toute fin (une fois qu'on
        // a suffisamment d'évènements pour renvoyer le résultat attendu).

        // Cela nous permet de gagner en performances tout en restant pertinent
        // vis à vis des données attendues. L'inconvénient est que le plus on
        // remonte dans le passé, plus le temps de traitement augmente.

        // Mais on part du principe que les évènements les plus consultés sont
        // les derniers évènements en date et cette logique ne s'applique a
        // priori que dans le cas des mises à jour d'une plateforme.

        String platformId = findPlatformId(platformKey);

        List<PlatformEventView> platformEvents = new ArrayList<>();
        List<EventView> rawEvents = new ArrayList<>();
        // On s'arrête lorsqu'on a assez d'évènements de mises à jour de la
        // plateforme à retourner, tenant compte de la pagination
        for (int queryPage = 1; platformEvents.size() < (clientPage * size); queryPage++) {

            List<EventView> newRawEvents = eventQueries.getLastToFirstEventsByTypes(
                    platformId, new Class[]{PlatformCreatedEvent.class, PlatformUpdatedEvent.class},
                    queryPage,
                    eventsQuerySizeFactor * size);

            if (isEmpty(newRawEvents)) {
                break;
            }
            rawEvents.addAll(newRawEvents);
            platformEvents = PlatformEventView.buildPlatformEvents(rawEvents);
        }
        log.debug("${queryPage} querie(s) of ${eventsQuerySizeFactor * size} platform events " +
                "to get ${size} elements for page ${clientPage} of platform update events");
        // Tri et pagination appliqués à la toute fin
        return platformEvents
                .stream()
                .sorted(comparing(PlatformEventView::getTimestamp).reversed())
                .skip((clientPage - 1) * size)
                .limit(size)
                .collect(toList());
    }

    public List<PropertiesEventView> getPropertiesEvents(User user,
                                                         Platform.Key platformKey,
                                                         String propertiesPath,
                                                         Integer page,
                                                         Integer size) {

        String platformId = findPlatformId(platformKey);
        boolean isModuleProperties = !GLOBAL_PROPERTIES_PATH.equals(propertiesPath);
        boolean isProductionPlatform = isModuleProperties && platformQueries.isProductionPlatform(platformId);
        List<PropertiesEventView> propertiesEvents = new ArrayList<>();

        boolean hidePasswords = isModuleProperties && isRestrictedPlatform(isProductionPlatform, user, platformKey.getApplicationName());
        List<EventView> events = isModuleProperties
                ? eventQueries.getLastToFirstPlatformModulePropertiesUpdatedEvents(platformId, propertiesPath, page, size)
                : eventQueries.getLastToFirstEventsByType(platformId, PlatformPropertiesUpdatedEvent.class, page, size);

        if (!isEmpty(events)) {
            // On récupère le premier évènement de la page suivante pour faire
            // la comparaison avec le dernier élément de la liste en cours
            Optional<EventView> firstEventOfNextPage = (isModuleProperties
                    ? eventQueries.getLastToFirstPlatformModulePropertiesUpdatedEvents(platformId, propertiesPath, page + 1, size)
                    : eventQueries.getLastToFirstEventsByType(platformId, PlatformPropertiesUpdatedEvent.class, page + 1, size)
            ).stream().findFirst();
            // On en profite pour déterminer si le premier évènement
            // en date fait partie de la liste en cours
            boolean containsFirstEvent = true;
            if (firstEventOfNextPage.isPresent()) {
                events.add(firstEventOfNextPage.get());
                containsFirstEvent = false;
            }

            List<String> passwordProperties = findPasswordProperties(hidePasswords, propertiesPath);
            propertiesEvents = PropertiesEventView.buildPropertiesEvents(events, isModuleProperties, containsFirstEvent).stream()
                    .map(propertiesEvent -> hidePasswords ? propertiesEvent.hidePasswords(passwordProperties) : propertiesEvent)
                    .sorted(comparing(PropertiesEventView::getTimestamp).reversed())
                    .collect(toList());
        }
        return propertiesEvents;
    }

    private List<String> findPasswordProperties(boolean hidePasswords, String propertiesPath) {
        List<String> passwordProperties = new ArrayList<>();
        if (hidePasswords) {
            Module.Key moduleKey = Module.Key.fromPropertiesPath(propertiesPath);
            List<AbstractPropertyView> moduleProperties = moduleQueries.getPropertiesModel(moduleKey);
            passwordProperties = AbstractPropertyView.getAllSimpleProperties(moduleProperties)
                    .filter(PropertyView::isPassword)
                    .map(PropertyView::getName)
                    .collect(toUnmodifiableList());
        }
        return passwordProperties;
    }

    private static boolean isRestrictedPlatform(User user, PlatformView platform) {
        return isRestrictedPlatform(platform.isProductionPlatform(), user, platform.getApplicationName());
    }

    private static boolean isRestrictedPlatform(User user, Platform platform) {
        return isRestrictedPlatform(platform.isProductionPlatform(), user, platform.getKey().getApplicationName());
    }

    private static boolean isRestrictedPlatform(boolean isProductionPlatform, User user, String applicationName) {
        return isProductionPlatform && !user.hasProductionRoleForApplication(applicationName);
    }

    private String findPlatformId(Platform.Key platformKey) {
        return platformQueries.getOptionalPlatformId(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
    }
}
