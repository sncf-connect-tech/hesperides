package org.hesperides.core.application.platforms;

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.exceptions.ForbiddenOperationException;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.modules.queries.ModuleSimplePropertiesView;
import org.hesperides.core.domain.platforms.commands.PlatformCommands;
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.exceptions.*;
import org.hesperides.core.domain.platforms.queries.PlatformQueries;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.GlobalPropertyUsageView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.security.User;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.isBlank;


@Component
public class PlatformUseCases {

    public static final String ROOT_PATH = "#";
    private final PlatformCommands commands;
    private final PlatformQueries queries;
    private final ModuleQueries moduleQueries;

    @Autowired
    public PlatformUseCases(PlatformCommands commands, PlatformQueries queries, final ModuleQueries moduleQueries) {
        this.commands = commands;
        this.queries = queries;
        this.moduleQueries = moduleQueries;
    }

    public String createPlatform(Platform platform, User user) {
        if (platform.isProductionPlatform() && !user.isProd()) {
            throw new ForbiddenOperationException("Creating a production platform is reserved to production role");
        }
        if (queries.platformExists(platform.getKey())) {
            throw new DuplicatePlatformException(platform.getKey());
        }
        return commands.createPlatform(platform, user);
    }

    public String copyPlatform(Platform newPlatform, Platform.Key existingPlatformKey, boolean copyInstancesAndProperties, User user) {
        if (queries.platformExists(newPlatform.getKey())) {
            throw new DuplicatePlatformException(newPlatform.getKey());
        }
        PlatformView existingPlatform = queries.getOptionalPlatform(existingPlatformKey)
                .orElseThrow(() -> new PlatformNotFoundException(existingPlatformKey));
        if ((newPlatform.isProductionPlatform() || existingPlatform.isProductionPlatform()) && !user.isProd()) {
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
                existingPlatform.getGlobalPropertiesVersionId(),
                globalProperties
        );
        return commands.createPlatform(newFullPlatform, user);
    }

    public PlatformView getPlatform(String platformId) {
        return queries.getOptionalPlatform(platformId)
                .orElseThrow(() -> new PlatformNotFoundException(platformId));
    }

    public PlatformView getPlatform(Platform.Key platformKey) {
        return queries.getOptionalPlatform(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
    }

    public PlatformView getPlatformAtPointInTime(Platform.Key platformKey, long timestamp) {
        String platformId = queries.getOptionalPlatformId(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
        return queries.getPlatformAtPointInTime(platformId, timestamp);
    }

    public void updatePlatform(Platform.Key platformKey, Platform newPlatform, boolean copyPropertiesForUpgradedModules, User user) {
        PlatformView existingPlatform = queries.getOptionalPlatform(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
        List<DeployedModule> existingDeployedModule = DeployedModuleView.toDomainDeployedModules(existingPlatform.getDeployedModules().stream());
        newPlatform = newPlatform.fillDeployedModulesMissingPropertiesVersionIds(existingDeployedModule);

        if (!user.isProd()) {
            if (existingPlatform.isProductionPlatform() && newPlatform.isProductionPlatform()) {
                throw new ForbiddenOperationException("Updating a production platform is reserved to production role");
            }
            if (existingPlatform.isProductionPlatform() || newPlatform.isProductionPlatform()) {
                throw new ForbiddenOperationException("Upgrading a platform to production is reserved to production role");
            }
        }
        commands.updatePlatform(existingPlatform.getId(), newPlatform, copyPropertiesForUpgradedModules, user);
    }

    public void deletePlatform(Platform.Key platformKey, User user) {
        PlatformView platform = queries.getOptionalPlatform(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
        if (platform.isProductionPlatform() && !user.isProd()) {
            throw new ForbiddenOperationException("Deleting a production platform is reserved to production role");
        }
        commands.deletePlatform(platform.getId(), platformKey, user);
    }

    public ApplicationView getApplication(String applicationName) {
        return queries.getApplication(applicationName)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationName));
    }

    public List<ModulePlatformView> getPlatformsUsingModule(Module.Key moduleKey) {
        return queries.getPlatformsUsingModule(moduleKey);
    }

    public List<SearchApplicationResultView> getApplicationNames() {
        return queries.getApplicationNames();
    }

    public List<SearchApplicationResultView> searchApplications(String applicationName) {
        List<SearchApplicationResultView> applications;
        if (isBlank(applicationName)) {
            applications = queries.getApplicationNames();
        } else {
            applications = queries.searchApplications(applicationName);
        }
        return applications;
    }

    public List<SearchPlatformResultView> searchPlatforms(String applicationName, String platformName) {
        return queries.searchPlatforms(applicationName, platformName);
    }

    public Long getPropertiesVersionId(final Platform.Key platformKey, final String propertiesPath) {
        return getPropertiesVersionId(platformKey, propertiesPath, null);
    }

    public Long getPropertiesVersionId(final Platform.Key platformKey, final String propertiesPath, final Long timestamp) {

        Optional<Long> propertiesVersionId = Optional.empty();

        if (ROOT_PATH.equals(propertiesPath)) {
            propertiesVersionId = queries.getGlobalPropertiesVersionId(platformKey);
        } else if (StringUtils.isNotEmpty(propertiesPath)) {
            final String platformId = queries.getOptionalPlatformId(platformKey).orElseThrow(() -> new PlatformNotFoundException(platformKey));
            propertiesVersionId = Optional.ofNullable(queries.getPropertiesVersionId(platformId, propertiesPath, timestamp));
        }
        return propertiesVersionId.orElse(DeployedModule.INIT_PROPERTIES_VERSION_ID);
    }

    public List<AbstractValuedPropertyView> getValuedProperties(final Platform.Key platformKey, final String propertiesPath, final User user) {
        return getValuedProperties(platformKey, propertiesPath, null, user);
    }

    public List<AbstractValuedPropertyView> getValuedProperties(final Platform.Key platformKey, final String propertiesPath, final Long timestamp, final User user) {
        List<AbstractValuedPropertyView> properties = new ArrayList<>();

        PlatformView platform = timestamp != null ? getPlatformAtPointInTime(platformKey, timestamp) : getPlatform(platformKey);

        if (ROOT_PATH.equals(propertiesPath)) {
            properties.addAll(queries.getGlobalProperties(platformKey));
        } else if (StringUtils.isNotEmpty(propertiesPath)) {
            final Module.Key moduleKey = Module.Key.fromPropertiesPath(propertiesPath);
            if (!moduleQueries.moduleExists(moduleKey)) {
                throw new ModuleNotFoundException(moduleKey);
            }
            List<AbstractPropertyView> modulePropertiesModel = moduleQueries.getPropertiesModel(moduleKey);

            properties = queries.getDeployedModuleProperties(platform.getId(), propertiesPath, timestamp);

            // On exclue les propriétés non valorisées ayant une valeur par défaut
            properties = AbstractValuedPropertyView.excludePropertiesWithOnlyDefaultValue(properties, modulePropertiesModel);

            // Pas besoin de récupérer la plateforme entière juste pour ce test
            // surtout si c'est pour refaire une requête pour récupérer les propriétés
            // => Créer une requête isProductionPlatform ou réutiliser la plateforme
            // pour récupérer les propriétés
            if (platform.isProductionPlatform() && !user.isProd()) {
                properties = AbstractValuedPropertyView.hidePasswordProperties(properties, modulePropertiesModel);
            }
        }
        return properties;
    }

    public Map<String, Set<GlobalPropertyUsageView>> getGlobalPropertiesUsage(final Platform.Key platformKey) {
        PlatformView platform = queries.getOptionalPlatform(platformKey).orElseThrow(() -> new PlatformNotFoundException(platformKey));

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
        if (!queries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }
        return queries.getInstancesModel(platformKey, propertiesPath);
    }

    public List<AbstractValuedPropertyView> saveProperties(final Platform.Key platformKey,
                                                           final String propertiesPath,
                                                           final Long platformVersionId,
                                                           final List<AbstractValuedProperty> abstractValuedProperties,
                                                           final Long propertiesVersionId,
                                                           final User user) {
        Optional<PlatformView> optPlatform = queries.getOptionalPlatform(platformKey);
        if (!optPlatform.isPresent()) {
            throw new PlatformNotFoundException(platformKey);
        }
        PlatformView platform = optPlatform.get();
        if (platform.isProductionPlatform() && !user.isProd()) {
            throw new ForbiddenOperationException("Setting properties of a production platform is reserved to production role");
        }

        Long expectedPropertiesVersionId = getPropertiesVersionId(platformKey, propertiesPath);

        if (ROOT_PATH.equals(propertiesPath)) {
            List<ValuedProperty> valuedProperties = AbstractValuedProperty.filterAbstractValuedPropertyWithType(abstractValuedProperties, ValuedProperty.class);
            // Platform properties are global and should always be of type ValuedProperty
            if (valuedProperties.size() != abstractValuedProperties.size()) {
                throw new IllegalArgumentException("Global properties should always be valued properties");
            }
            commands.savePlatformProperties(platform.getId(), platformVersionId, propertiesVersionId, expectedPropertiesVersionId, valuedProperties, user);
        } else {
            final Module.Key moduleKey = Module.Key.fromPropertiesPath(propertiesPath);
            if (!moduleQueries.moduleExists(moduleKey)) {
                throw new ModuleNotFoundException(moduleKey);
            }
            validateRequiredAndPatternProperties(abstractValuedProperties, moduleKey, platformKey);
            commands.saveModulePropertiesInPlatform(platform.getId(), propertiesPath, platformVersionId, propertiesVersionId, expectedPropertiesVersionId, abstractValuedProperties, user);
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
        List<ValuedPropertyView> globalProperties = queries.getGlobalProperties(platformKey);
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
        if (queries.platformExists(platformKey)) {
            throw new IllegalArgumentException("Cannot restore an existing platform");
        }
        String platformId = queries.getOptionalPlatformIdFromEvents(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
        commands.restoreDeletedPlatform(platformId, user);
        return queries.getOptionalPlatform(platformId).get();
    }

    public List<ApplicationView> getAllApplicationsDetail() {
        return queries.getAllApplicationsDetail();
    }
}
