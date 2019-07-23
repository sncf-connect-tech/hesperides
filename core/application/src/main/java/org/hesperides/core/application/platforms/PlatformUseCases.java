package org.hesperides.core.application.platforms;

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.exceptions.ForbiddenOperationException;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.modules.queries.ModuleSimplePropertiesView;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.platforms.commands.PlatformCommands;
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitorsSequence;
import org.hesperides.core.domain.platforms.exceptions.*;
import org.hesperides.core.domain.platforms.queries.PlatformQueries;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.GlobalPropertyUsageView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.security.queries.ApplicationDirectoryGroupsQueries;
import org.hesperides.core.domain.security.queries.views.ApplicationDirectoryGroupsView;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.technos.queries.TechnoQueries;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateContainerKeyView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.hesperides.core.application.properties.PropertyUseCases.buildPropertyVisitorsSequence;


@Component
public class PlatformUseCases {

    public static final String ROOT_PATH = "#";
    private final PlatformCommands platformCommands;
    private final PlatformQueries platformQueries;
    private final ModuleQueries moduleQueries;
    private final TechnoQueries technoQueries;
    private final ApplicationDirectoryGroupsQueries applicationDirectoryGroupsQueries;

    public static enum PROPERTIES_WITH_ONLY_DEFAULT_VALUE {INCLUDE, EXCLUDE};


    @Autowired
    public PlatformUseCases(PlatformCommands platformCommands, PlatformQueries platformQueries, final ModuleQueries moduleQueries, TechnoQueries technoQueries, ApplicationDirectoryGroupsQueries applicationDirectoryGroupsQueries) {
        this.platformCommands = platformCommands;
        this.platformQueries = platformQueries;
        this.moduleQueries = moduleQueries;
        this.technoQueries = technoQueries;
        this.applicationDirectoryGroupsQueries = applicationDirectoryGroupsQueries;
    }

    public String createPlatform(Platform platform, User user) {
        if (platform.isProductionPlatform() && !user.isGlobalProd()) {
            throw new ForbiddenOperationException("Creating a production platform is reserved to production role");
        }
        if (platformQueries.platformExists(platform.getKey())) {
            throw new DuplicatePlatformException(platform.getKey());
        }
        return platformCommands.createPlatform(platform, user);
    }

    public String copyPlatform(Platform newPlatform, Platform.Key existingPlatformKey, boolean copyInstancesAndProperties, User user) {
        if (platformQueries.platformExists(newPlatform.getKey())) {
            throw new DuplicatePlatformException(newPlatform.getKey());
        }
        PlatformView existingPlatform = platformQueries.getOptionalPlatform(existingPlatformKey)
                .orElseThrow(() -> new PlatformNotFoundException(existingPlatformKey));
        if ((newPlatform.isProductionPlatform() || existingPlatform.isProductionPlatform()) && !user.isGlobalProd()) {
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
                globalProperties
        );
        return platformCommands.createPlatform(newFullPlatform, user);
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
        if (!user.isGlobalProd()) {
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
        if (platform.isProductionPlatform() && !user.isGlobalProd()) {
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

    public PropertiesDiff getPropertiesDiff(final Platform.Key fromPlatformKey,
                                            final String fromPropertiesPath,
                                            final String fromInstanceName,
                                            final Platform.Key toPlatformKey,
                                            final String toPropertiesPath,
                                            final String toInstanceName,
                                            final Long timestamp,
                                            final User user) {
        Module.Key fromModuleKey = Module.Key.fromPropertiesPath(fromPropertiesPath);
        String fromModulePath = extractModulePathFromPropertiesPath(fromPropertiesPath);
        if (!queries.deployedModuleExists(fromPlatformKey, fromModuleKey, fromModulePath)) {
            throw new DeployedModuleNotFoundException(fromPlatformKey, fromModuleKey, fromModulePath);
        }
        Module.Key toModuleKey = Module.Key.fromPropertiesPath(toPropertiesPath);
        String toModulePath = extractModulePathFromPropertiesPath(toPropertiesPath);
        if (!queries.deployedModuleExists(toPlatformKey, toModuleKey, toModulePath)) {
            throw new DeployedModuleNotFoundException(toPlatformKey, toModuleKey, toModulePath);
        }

        PlatformView fromPlatform = queries.getOptionalPlatform(fromPlatformKey)
                .orElseThrow(() -> new PlatformNotFoundException(fromPlatformKey));
        PlatformView toPlatform = queries.getOptionalPlatform(toPlatformKey)
                .orElseThrow(() -> new PlatformNotFoundException(toPlatformKey));

        List<AbstractPropertyView> fromModulePropertiesModels = moduleQueries.getPropertiesModel(fromModuleKey);
        List<AbstractPropertyView> toModulePropertiesModels = moduleQueries.getPropertiesModel(toModuleKey);

        boolean fromShouldHidePasswordProperties = fromPlatform.isProductionPlatform() && !user.isProd();
        boolean toShouldHidePasswordProperties = toPlatform.isProductionPlatform() && !user.isProd();

        PropertyVisitorsSequence fromPropertyVisitors = buildPropertyVisitorsSequence(
                fromPlatform, fromModulePath, fromModuleKey,
                fromModulePropertiesModels,
                fromInstanceName, fromShouldHidePasswordProperties, true);
        PropertyVisitorsSequence toPropertyVisitors = buildPropertyVisitorsSequence(
                toPlatform, toModulePath, toModuleKey,
                toModulePropertiesModels,
                toInstanceName, toShouldHidePasswordProperties, true);
        return PropertyVisitorsSequence.performDiff(fromPropertyVisitors, toPropertyVisitors);
    }

    private static String extractModulePathFromPropertiesPath(String propertiesPath) {
        String[] parts = propertiesPath.split("#");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Too short properties path: " + propertiesPath);
        }
        parts = Arrays.copyOfRange(parts, 0, parts.length - 3);
        return String.join("#", parts);
    }

    public List<AbstractValuedPropertyView> getValuedProperties(final Platform.Key platformKey, final String propertiesPath, final User user) {
        return getValuedProperties(platformKey, propertiesPath, null, user);
    }

    public List<AbstractValuedPropertyView> getValuedProperties(final Platform.Key platformKey, final String propertiesPath, final Long timestamp, final User user) {
        return getValuedProperties(platformKey, propertiesPath, timestamp, user, PROPERTIES_WITH_ONLY_DEFAULT_VALUE.EXCLUDE);
    }

    public List<AbstractValuedPropertyView> getValuedProperties(final Platform.Key platformKey, final String propertiesPath, final Long timestamp, final User user, final PROPERTIES_WITH_ONLY_DEFAULT_VALUE propertiesWithOnlyDefaultValue) {
        List<AbstractValuedPropertyView> properties = new ArrayList<>();

        PlatformView platform = timestamp != null ? getPlatformAtPointInTime(platformKey, timestamp) : getPlatform(platformKey);

        if (ROOT_PATH.equals(propertiesPath)) {
            properties.addAll(platformQueries.getGlobalProperties(platformKey));
        } else if (StringUtils.isNotEmpty(propertiesPath)) {
            final Module.Key moduleKey = Module.Key.fromPropertiesPath(propertiesPath);
            if (!moduleQueries.moduleExists(moduleKey)) {
                throw new ModuleNotFoundException(moduleKey);
            }
            List<AbstractPropertyView> modulePropertiesModel = moduleQueries.getPropertiesModel(moduleKey);

            properties = platformQueries.getDeployedModuleProperties(platform.getId(), propertiesPath, timestamp);

            if (PROPERTIES_WITH_ONLY_DEFAULT_VALUE.EXCLUDE.equals(propertiesWithOnlyDefaultValue)) {
                // On exclue les propriétés non valorisées ayant une valeur par défaut
                properties = AbstractValuedPropertyView.excludePropertiesWithOnlyDefaultValue(properties, modulePropertiesModel);
            }

            // Pas besoin de récupérer la plateforme entière juste pour ce test
            // surtout si c'est pour refaire une requête pour récupérer les propriétés
            // => Créer une requête isProductionPlatform ou réutiliser la plateforme
            // pour récupérer les propriétés
            if (platform.isProductionPlatform() && !user.isGlobalProd()) {
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
        List<ModuleSimplePropertiesView> modulesSimpleProperties = moduleQueries.getModulesSimpleProperties(modulesKeys);

        return platform.getGlobalProperties().stream()
                .map(ValuedPropertyView::getName)
                .collect(Collectors.toMap(Function.identity(), globalPropertyName ->
                        GlobalPropertyUsageView.getGlobalPropertyUsage(globalPropertyName, deployedModules, modulesSimpleProperties)));
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
                                                           final User user) {
        Optional<PlatformView> optPlatform = platformQueries.getOptionalPlatform(platformKey);
        if (!optPlatform.isPresent()) {
            throw new PlatformNotFoundException(platformKey);
        }
        PlatformView platform = optPlatform.get();
        if (platform.isProductionPlatform() && !user.isGlobalProd()) {
            throw new ForbiddenOperationException("Setting properties of a production platform is reserved to production role");
        }
        if (ROOT_PATH.equals(propertiesPath)) {
            List<ValuedProperty> valuedProperties = AbstractValuedProperty.filterAbstractValuedPropertyWithType(abstractValuedProperties, ValuedProperty.class);
            // Platform properties are global and should always be of type ValuedProperty
            if (valuedProperties.size() != abstractValuedProperties.size()) {
                throw new IllegalArgumentException("Global properties should always be valued properties");
            }
            platformCommands.savePlatformProperties(platform.getId(), platformVersionId, valuedProperties, user);
        } else {
            final Module.Key moduleKey = Module.Key.fromPropertiesPath(propertiesPath);
            if (!moduleQueries.moduleExists(moduleKey)) {
                throw new ModuleNotFoundException(moduleKey);
            }
            validateRequiredAndPatternProperties(abstractValuedProperties, moduleKey, platformKey);
            platformCommands.saveModulePropertiesInPlatform(platform.getId(), propertiesPath, platformVersionId, abstractValuedProperties, user);
        }

        return getValuedProperties(platformKey, propertiesPath, user);
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

        AbstractPropertyView.getFlatProperties(moduleQueries.getPropertiesModel(moduleKey)).forEach(moduleProperty -> {
            List<ValuedProperty> matchingValuedProperties = allValuedProperties.stream()
                    .filter(valuedPropery -> StringUtils.equals(valuedPropery.getName(), moduleProperty.getName()))
                    .collect(Collectors.toList());
            if (moduleProperty.isRequiredAndNotValorised(matchingValuedProperties)) {
                throw new RequiredPropertyNotValorisedException(moduleProperty.getName());
            } else if (moduleProperty.hasValueThatDoesntMatchPattern(matchingValuedProperties)) {
                throw new PropertyPatternNotMatchedException(moduleProperty.getName(), moduleProperty.getPattern());
            }
        });
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
}
