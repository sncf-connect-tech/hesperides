package org.hesperides.core.application.platforms;

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.application.platforms.properties.PropertyType;
import org.hesperides.core.application.platforms.properties.PropertyValuationBuilder;
import org.hesperides.core.application.platforms.properties.PropertyValuationContext;
import org.hesperides.core.domain.events.commands.EventCommands;
import org.hesperides.core.domain.exceptions.ForbiddenOperationException;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.queries.ModulePropertiesView;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.platforms.commands.PlatformCommands;
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff.ComparisonMode;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitorsSequence;
import org.hesperides.core.domain.platforms.exceptions.ApplicationNotFoundException;
import org.hesperides.core.domain.platforms.exceptions.DuplicatePlatformException;
import org.hesperides.core.domain.platforms.exceptions.PlatformNotFoundException;
import org.hesperides.core.domain.platforms.queries.PlatformQueries;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.platforms.queries.views.properties.*;
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
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.hesperides.core.application.platforms.properties.PropertyType.GLOBAL;
import static org.hesperides.core.application.platforms.properties.PropertyType.WITHOUT_MODEL;
import static org.hesperides.core.application.platforms.properties.PropertyValuationBuilder.buildPropertyVisitorsSequenceForGlobals;
import static org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView.excludeUnusedValues;

@Component
public class PlatformUseCases {

    private final PlatformCommands platformCommands;
    private final PlatformQueries platformQueries;
    private final ModuleQueries moduleQueries;
    private final TechnoQueries technoQueries;
    private final ApplicationDirectoryGroupsQueries applicationDirectoryGroupsQueries;
    private final EventCommands eventCommands;
    private final PropertyReferenceScanner propertyReferenceScanner;

    @Autowired
    public PlatformUseCases(PlatformCommands platformCommands,
                            PlatformQueries platformQueries,
                            ModuleQueries moduleQueries,
                            TechnoQueries technoQueries,
                            ApplicationDirectoryGroupsQueries applicationDirectoryGroupsQueries,
                            EventCommands eventCommands,
                            PropertyReferenceScanner propertyReferenceScanner) {
        this.platformCommands = platformCommands;
        this.platformQueries = platformQueries;
        this.moduleQueries = moduleQueries;
        this.technoQueries = technoQueries;
        this.applicationDirectoryGroupsQueries = applicationDirectoryGroupsQueries;
        this.eventCommands = eventCommands;
        this.propertyReferenceScanner = propertyReferenceScanner;
    }

    public String createPlatform(Platform platform, User user) {
        if (platform.isProductionPlatform() && !user.hasProductionRoleForApplication(platform.getKey().getApplicationName())) {
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
        List<DeployedModule> deployedModules = DeployedModuleView.toDomainDeployedModules(existingPlatform.getActiveDeployedModules());
        if (!copyInstancesAndProperties) {
            deployedModules = deployedModules.stream()
                    .map(DeployedModule::copyWithoutInstancesNorProperties)
                    .collect(Collectors.toList());
        }
        List<ValuedProperty> globalProperties = copyInstancesAndProperties ? ValuedPropertyView.toDomainValuedProperties(existingPlatform.getGlobalProperties()) : Collections.emptyList();
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
            boolean hasPasswords = !CollectionUtils.isEmpty(getPlatformsWithPassword(Collections.singletonList(platform)));
            platform = platform.withPasswordIndicator(hasPasswords);
        }
        return platform;
    }

    public PlatformView getPlatformAtPointInTime(Platform.Key platformKey, long timestamp) {
        String platformId = platformQueries.getOptionalPlatformId(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
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
        if (platform.isProductionPlatform() && !user.hasProductionRoleForApplication(platformKey.getApplicationName())) {
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
                .collect(Collectors.toList());

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
                .collect(Collectors.toList());

        Map<Module.Key, List<Techno.Key>> technoKeysByModuleMap = allPlatformsModules.stream().collect(Collectors.toMap(
                ModuleView::getKey,
                module -> module.getTechnos().stream()
                        .map(TechnoView::getKey)
                        .map(TemplateContainerKeyView::toTechnoKey)
                        .distinct()
                        .collect(Collectors.toList())
        ));

        List<Techno.Key> technosWithPassword = technoQueries.getTechnosWithPasswordWithin(allModulesTechnoKeys);

        // Récupère la liste des modules ayant au moins une techno contenant un mot de passe
        // et la concatène avec la liste de modules contenant au moins un mot de passe
        Set<Module.Key> allModulesWithPassword = Stream.concat(modulesWithPassword.stream(),
                technoKeysByModuleMap.entrySet().stream()
                        .filter(entry -> entry.getValue().stream().anyMatch(technosWithPassword::contains))
                        .map(Map.Entry::getKey))
                .collect(Collectors.toSet());

        Map<Platform.Key, List<Module.Key>> moduleKeysByPlatformMap = platforms.stream().collect(Collectors.toMap(
                PlatformView::getPlatformKey,
                platform -> platform.getDeployedModules().stream()
                        .map(DeployedModuleView::getModuleKey)
                        .distinct()
                        .collect(Collectors.toList())
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

    public Long getPropertiesVersionId(final Platform.Key platformKey, final String propertiesPath) {
        return getPropertiesVersionId(platformKey, propertiesPath, null);
    }

    public Long getPropertiesVersionId(final Platform.Key platformKey, final String propertiesPath, final Long timestamp) {

        Optional<Long> propertiesVersionId = Optional.empty();

        if (Platform.isGlobalPropertiesPath(propertiesPath)) {
            propertiesVersionId = platformQueries.getGlobalPropertiesVersionId(platformKey);
        } else if (StringUtils.isNotEmpty(propertiesPath)) {
            final String platformId = platformQueries.getOptionalPlatformId(platformKey).orElseThrow(() -> new PlatformNotFoundException(platformKey));
            propertiesVersionId = Optional.ofNullable(platformQueries.getPropertiesVersionId(platformId, propertiesPath, timestamp));
        }
        return propertiesVersionId.orElse(DeployedModule.INIT_PROPERTIES_VERSION_ID);
    }

    public PropertiesDiff getPropertiesDiff(final Platform.Key fromPlatformKey,
                                            final String fromPropertiesPath,
                                            final String fromInstanceName,
                                            final Platform.Key toPlatformKey,
                                            final String toPropertiesPath,
                                            final String toInstanceName,
                                            final Long timestamp,
                                            final ComparisonMode comparisonMode,
                                            final User user) {

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

            boolean fromShouldHidePasswordProperties = fromPlatform.isProductionPlatform() && !user.hasProductionRoleForApplication(fromPlatformKey.getApplicationName());
            boolean toShouldHidePasswordProperties = toPlatform.isProductionPlatform() && !user.hasProductionRoleForApplication(toPlatformKey.getApplicationName());

            PropertyVisitorsSequence fromPropertyVisitors = buildModulePropertyVisitorsSequence(
                    fromPlatform, fromModulePath, fromModuleKey,
                    fromModulePropertiesModels,
                    fromInstanceName, fromShouldHidePasswordProperties);
            PropertyVisitorsSequence toPropertyVisitors = buildModulePropertyVisitorsSequence(
                    toPlatform, toModulePath, toModuleKey,
                    toModulePropertiesModels,
                    toInstanceName, toShouldHidePasswordProperties);

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
        DeployedModuleView deployedModule = platform.getDeployedModule(modulePath, moduleKey);

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

    public List<PropertyWithDetailsView> getPropertiesWithDetails(Platform.Key platformKey, String propertiesPath, User user) {

        PropertyVisitorsSequence propertyVisitorsSequence;
        PlatformView extractedPlatform = getPlatform(platformKey);
        if (Platform.isGlobalPropertiesPath(propertiesPath)) {
            propertyVisitorsSequence = buildPropertyVisitorsSequenceForGlobals(extractedPlatform);
        } else {
            String extractedModule = extractModulePathFromPropertiesPath(propertiesPath);
            Module.Key moduleKey = Module.Key.fromPropertiesPath(propertiesPath);
            List<AbstractPropertyView> modulePropertiesModel = moduleQueries.getPropertiesModel(moduleKey);
            boolean fromShouldHidePasswordProperties = extractedPlatform.isProductionPlatform() && !user.hasProductionRoleForApplication(platformKey.getApplicationName());

            propertyVisitorsSequence = buildModulePropertyVisitorsSequence(
                    extractedPlatform, extractedModule, moduleKey,
                    modulePropertiesModel,
                    null, fromShouldHidePasswordProperties);
        }
        return propertyVisitorsSequence.getPropertiesWithDetails();
    }

    public List<AbstractValuedPropertyView> getValuedProperties(final Platform.Key platformKey, final String propertiesPath, final Long timestamp, final User user) {
        List<AbstractValuedPropertyView> properties = new ArrayList<>();

        PlatformView platform = timestamp != null ? getPlatformAtPointInTime(platformKey, timestamp) : getPlatform(platformKey);

        if (Platform.isGlobalPropertiesPath(propertiesPath)) {
            properties.addAll(platform.getGlobalProperties());
        } else if (StringUtils.isNotEmpty(propertiesPath)) {
            final Module.Key moduleKey = Module.Key.fromPropertiesPath(propertiesPath);
            // Note: on devrait passer le timestamp aux 2 appels ci-dessous, cf. issue #724
            List<AbstractPropertyView> modulePropertiesModel = moduleQueries.getPropertiesModel(moduleKey);

            properties = platform.getDeployedModule(propertiesPath).getValuedProperties();

            // On exclue les propriétés non valorisées ayant une valeur par défaut
            properties = AbstractValuedPropertyView.excludePropertiesWithOnlyDefaultValue(properties, modulePropertiesModel);

            // Pas besoin de récupérer la plateforme entière juste pour ce test
            // surtout si c'est pour refaire une requête pour récupérer les propriétés
            // => Créer une requête isProductionPlatform ou réutiliser la plateforme
            // pour récupérer les propriétés
            if (platform.isProductionPlatform() && !user.hasProductionRoleForApplication(platformKey.getApplicationName())) {
                properties = AbstractValuedPropertyView.hidePasswordProperties(properties, modulePropertiesModel);
            }
        }
        return properties;
    }

    public Map<String, Set<GlobalPropertyUsageView>> getGlobalPropertiesUsage(final Platform.Key platformKey) {
        PlatformView platform = platformQueries.getOptionalPlatform(platformKey).orElseThrow(() -> new PlatformNotFoundException(platformKey));

        // On ne tient compte que des modules utilisés dans la platforme (pas des modules sauvegardés)
        List<DeployedModuleView> deployedModules = platform.getActiveDeployedModules()
                .collect(Collectors.toList());

        List<TemplateContainer.Key> modulesKeys = deployedModules
                .stream()
                .map(DeployedModuleView::getModuleKey)
                .collect(Collectors.toList());

        List<ModulePropertiesView> modulesProperties = moduleQueries.getModulesProperties(modulesKeys);

        return platform.getGlobalProperties().stream()
                .map(ValuedPropertyView::getName)
                .collect(Collectors.toMap(Function.identity(), globalPropertyName ->
                        GlobalPropertyUsageView.getGlobalPropertyUsage(globalPropertyName, deployedModules, modulesProperties)));
    }

    public List<String> getInstancesModel(final Platform.Key platformKey, final String propertiesPath) {
        if (!platformQueries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }
        return platformQueries.getInstancesModel(platformKey, propertiesPath);
    }

    public List<AbstractValuedPropertyView> saveProperties(final Platform.Key platformKey,
                                                           final String propertiesPath,
                                                           final Long platformVersionId,
                                                           final List<AbstractValuedProperty> abstractValuedProperties,
                                                           final Long propertiesVersionId,
                                                           final String userComment,
                                                           final User user) {
        PlatformView platform = getPlatform(platformKey);
        if (platform.isProductionPlatform() && !user.hasProductionRoleForApplication(platformKey.getApplicationName())) {
            throw new ForbiddenOperationException("Setting properties of a production platform is reserved to production role");
        }

        if (containsDuplicateKeys(abstractValuedProperties)) {
            throw new IllegalArgumentException("Saving properties with duplicate keys is forbidden");
        }

        Long expectedPropertiesVersionId = getPropertiesVersionId(platformKey, propertiesPath);

        if (Platform.isGlobalPropertiesPath(propertiesPath)) {
            List<ValuedProperty> valuedProperties = AbstractValuedProperty.filterAbstractValuedPropertyWithType(abstractValuedProperties, ValuedProperty.class);
            // Platform properties are global and should always be of type ValuedProperty
            if (valuedProperties.size() != abstractValuedProperties.size()) {
                throw new IllegalArgumentException("Global properties should always be valued properties");
            }
            platformCommands.savePlatformProperties(platform.getId(), platformVersionId, propertiesVersionId, expectedPropertiesVersionId, valuedProperties, user);
        } else {
            final Module.Key moduleKey = Module.Key.fromPropertiesPath(propertiesPath);
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
                    userComment,
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

    public PlatformView restoreDeletedPlatform(final Platform.Key platformKey, final User user) {
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
                    .collect(Collectors.toList());

            Set<Platform.Key> platformsWithPassword = getPlatformsWithPassword(allApplicationsPlatforms);
            applications = applications.stream()
                    .map(application -> application.withPasswordIndicator(platformsWithPassword))
                    .collect(Collectors.toList());
        }

        return applications;
    }

    public void purgeUnusedProperties(Platform.Key platformKey, String propertiesPath, User user) {
        final PlatformView platform = getPlatform(platformKey);
        if (platform.isProductionPlatform() && !user.hasProductionRoleForApplication(platformKey.getApplicationName())) {
            throw new ForbiddenOperationException("Cleaning properties of a production platform is reserved to production role");
        }
        if (Platform.isGlobalPropertiesPath(propertiesPath)) {
            throw new IllegalArgumentException("Cleaning only works on module properties (not global ones!)");
        }

        final DeployedModuleView deployedModule = platform.getDeployedModule(propertiesPath);
        final Module.Key moduleKey = Module.Key.fromPropertiesPath(propertiesPath);

        final List<AbstractPropertyView> propertiesModel = moduleQueries.getPropertiesModel(moduleKey);
        final List<AbstractValuedPropertyView> baseValues = deployedModule.getValuedProperties();
        final Set<String> referencedProperties = propertyReferenceScanner.findAll(baseValues, deployedModule.getInstances());

        List<AbstractValuedProperty> filteredValuedProperties = excludeUnusedValues(baseValues, propertiesModel, referencedProperties)
                .map(AbstractValuedPropertyView::toDomainValuedProperty)
                .map(AbstractValuedProperty.class::cast)
                .collect(Collectors.toList());

        Long propertiesVersionId = platformQueries.getPropertiesVersionId(platform.getId(), propertiesPath, null);

        platformCommands.saveModulePropertiesInPlatform(platform.getId(), propertiesPath, platform.getVersionId(),
                propertiesVersionId, propertiesVersionId, filteredValuedProperties,
                "Generated comment: cleaning unused properties", user);
    }

    private static boolean containsDuplicateKeys(List<AbstractValuedProperty> list) {
        return !CollectionUtils.isEmpty(list) && !list.stream()
                .map(AbstractValuedProperty::getName)
                .allMatch(new HashSet<>()::add);
    }

    private String cleanCreatePlatform(Platform platform, User user) {
        eventCommands.cleanAggregateEvents(platform.getKey().generateHash());
        return platformCommands.createPlatform(platform, user);
    }
}
