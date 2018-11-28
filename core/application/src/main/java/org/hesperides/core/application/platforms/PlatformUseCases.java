package org.hesperides.core.application.platforms;

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.platforms.commands.PlatformCommands;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.exceptions.ApplicationNotFoundException;
import org.hesperides.core.domain.platforms.exceptions.DuplicatePlatformException;
import org.hesperides.core.domain.platforms.exceptions.PlatformNotFoundException;
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
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


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
        if (queries.platformExists(platform.getKey())) {
            throw new DuplicatePlatformException(platform.getKey());
        }
        return commands.createPlatform(platform, user);
    }

    public String copyPlatform(Platform newPlatform, Platform.Key existingPlatformKey, User user) {
        if (!queries.platformExists(existingPlatformKey)) {
            throw new PlatformNotFoundException(existingPlatformKey);
        }
        if (queries.platformExists(newPlatform.getKey())) {
            throw new DuplicatePlatformException(newPlatform.getKey());
        }
        return commands.copyPlatform(existingPlatformKey, newPlatform, user);
    }

    public PlatformView getPlatform(String platformId) {
        return queries.getOptionalPlatform(platformId)
                .orElseThrow(() -> new PlatformNotFoundException(platformId));
    }

    public PlatformView getPlatform(Platform.Key platformKey) {
        return queries.getOptionalPlatform(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
    }

    public void updatePlatform(Platform.Key platformKey, Platform platform, boolean copyProperties, User user) {
        Optional<String> platformId = queries.getOptionalPlatformId(platformKey);
        if (!platformId.isPresent()) {
            throw new PlatformNotFoundException(platformKey);
        }
        commands.updatePlatform(platformId.get(), platform, copyProperties, user);
    }

    public void deletePlatform(Platform.Key platformKey, User user) {
        Optional<String> platformId = queries.getOptionalPlatformId(platformKey);
        if (!platformId.isPresent()) {
            throw new PlatformNotFoundException(platformKey);
        }
        commands.deletePlatform(platformId.get(), user);
    }

    public ApplicationView getApplication(String applicationName) {
        return queries.getApplication(applicationName)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationName));
    }

    public List<ModulePlatformView> getPlatformUsingModule(Module.Key moduleKey) {
        return queries.getPlatformsUsingModule(moduleKey);
    }

    public List<SearchPlatformResultView> searchPlatforms(String applicationName, String platformName) {
        return queries.searchPlatforms(applicationName, platformName);
    }

    public List<SearchApplicationResultView> searchApplications(String applicationName) {
        return queries.searchApplications(applicationName);
    }

    public List<AbstractValuedPropertyView> getProperties(final Platform.Key platformKey, final String path) {
        List<AbstractValuedPropertyView> properties = new ArrayList<>();

        if (!queries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }

        if (ROOT_PATH.equals(path)) {
            properties.addAll(queries.getGlobalProperties(platformKey));
        } else if (StringUtils.isNotEmpty(path)) {
            final Module.Key moduleKey = Module.Key.fromPath(path);
            if (!moduleQueries.moduleExists(moduleKey)) {
                throw new ModuleNotFoundException(moduleKey);
            }
            properties.addAll(queries.getDeployedModuleProperties(platformKey, path));
            properties.addAll(getGlobalPropertiesUsedInModule(platformKey, moduleKey));
        }
        return properties;
    }

    // TODO Cette méthode est à revoir car il y a peut-être moyen de factoriser ou de réutiliser le code du use case getGlobalPropertiesUsage
    private List<AbstractValuedPropertyView> getGlobalPropertiesUsedInModule(Platform.Key platformKey, Module.Key moduleKey) {
        List<AbstractValuedPropertyView> globalPropertiesUsedInModule = new ArrayList<>();

        PlatformView platform = queries.getOptionalPlatform(platformKey).get();
        Optional<DeployedModuleView> deployedModule = platform.getDeployedModule(moduleKey);
        if (deployedModule.isPresent()) {
            List<AbstractPropertyView> moduleProperties = moduleQueries.getProperties(moduleKey);
            List<AbstractPropertyView> flatModuleProperties = AbstractPropertyView.flattenProperties(moduleProperties);

            platform.getGlobalProperties().forEach(globalProperty -> {
                List<GlobalPropertyUsageView> moduleGlobalProperties = GlobalPropertyUsageView.getModuleGlobalProperties(
                        flatModuleProperties, globalProperty.getName(), deployedModule.get().getPropertiesPath());
                if (!CollectionUtils.isEmpty(moduleGlobalProperties)) {
                    globalPropertiesUsedInModule.add(globalProperty);
                }
            });
        }

        return globalPropertiesUsedInModule;
    }

    public List<InstancePropertyView> getInstanceModel(final Platform.Key platformKey, final String modulePath) {
        if (!queries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }
        return queries.getInstanceModel(platformKey, modulePath);
    }

    public List<AbstractValuedPropertyView> saveProperties(final Platform.Key platformKey,
                                                           final String path,
                                                           final Long platformVersionId,
                                                           final List<AbstractValuedProperty> abstractValuedProperties,
                                                           final User user) {
        Optional<String> platformId = queries.getOptionalPlatformId(platformKey);
        if (!platformId.isPresent()) {
            throw new PlatformNotFoundException(platformKey);
        }
        if (ROOT_PATH.equals(path)) {
            List<ValuedProperty> valuedProperties = AbstractValuedProperty.filterAbstractValuedPropertyWithType(abstractValuedProperties, ValuedProperty.class);
            // Platform properties are global and should always be of type ValuedProperty
            if (valuedProperties.size() != abstractValuedProperties.size()) {
                throw new IllegalArgumentException("Global properties should always be valued properties");
            }
            commands.savePlatformProperties(platformId.get(), platformVersionId, valuedProperties, user);
        } else {
            final Module.Key moduleKey = Module.Key.fromPath(path);
            if (!moduleQueries.moduleExists(moduleKey)) {
                throw new ModuleNotFoundException(moduleKey);
            }
            commands.saveModulePropertiesInPlatform(platformId.get(), path, platformVersionId, abstractValuedProperties, user);
        }

        return getProperties(platformKey, path);
    }

    public Map<String, Set<GlobalPropertyUsageView>> getGlobalPropertiesUsage(final Platform.Key platformKey) {

        return queries.getOptionalPlatform(platformKey)
                .map(platformView -> platformView.getGlobalProperties()
                        .stream()
                        .collect(Collectors.toMap(ValuedPropertyView::getName, globalProperty -> getGlobalPropertyUsage(platformView.getDeployedModules(), globalProperty))))
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
    }

    /**
     * Pour chaque propriété globale, on regarde si elle est utilisée.
     * Il existe 2 façons d'utiliser les propriétés globales :
     * - En tant que propriété dans un template
     * - En tant que valeur de propriété d'un module déployé
     */
    private Set<GlobalPropertyUsageView> getGlobalPropertyUsage(final List<DeployedModuleView> deployedModules, final ValuedPropertyView globalProperty) {
        final Set<GlobalPropertyUsageView> globalPropertyUsage = new HashSet<>();

        deployedModules.forEach(deployedModule -> {
            TemplateContainer.Key moduleKey = deployedModule.getModuleKey();

            final List<AbstractPropertyView> flatModuleProperties = new ArrayList<>();
            final boolean moduleExists = moduleQueries.moduleExists(moduleKey);

            if (moduleExists) {
                // Si le module existe, on récupère toutes ses propriétés, y compris les itérables
                // pouvant contenir elles-mêmes des propriétés, dans une liste "à plat".
                List<AbstractPropertyView> moduleProperties = moduleQueries.getProperties(moduleKey);
                flatModuleProperties.addAll(AbstractPropertyView.flattenProperties(moduleProperties));
                // Puis détermine les propriétés globales utilisées parmis ces propriétés de modules
                List<GlobalPropertyUsageView> moduleGlobalProperties = GlobalPropertyUsageView.getModuleGlobalProperties(
                        flatModuleProperties, globalProperty.getName(), deployedModule.getPropertiesPath());
                globalPropertyUsage.addAll(moduleGlobalProperties);
            }

            // Ici on extrait les proriétés globales utilisées lors de la valorisation des propriétés
            List<GlobalPropertyUsageView> deployedModuleGlobalProperties = GlobalPropertyUsageView.getDeployedModuleGlobalProperties(
                    deployedModule, globalProperty.getName(), flatModuleProperties, moduleExists);
            globalPropertyUsage.addAll(deployedModuleGlobalProperties);
        });
        return globalPropertyUsage;
    }
}
