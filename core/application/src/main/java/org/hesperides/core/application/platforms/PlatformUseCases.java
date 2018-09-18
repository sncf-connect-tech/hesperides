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
import org.hesperides.core.domain.platforms.queries.views.ApplicationView;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.platforms.queries.views.InstanceModelView;
import org.hesperides.core.domain.platforms.queries.views.ModulePlatformView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.platforms.queries.views.SearchApplicationResultView;
import org.hesperides.core.domain.platforms.queries.views.SearchPlatformResultView;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.GlobalPropertyUsageView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.security.User;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    public Platform.Key createPlatform(Platform platform, User user) {
        if (queries.platformExists(platform.getKey())) {
            throw new DuplicatePlatformException(platform.getKey());
        }
        return commands.createPlatform(platform, user);
    }

    public PlatformView getPlatform(Platform.Key platformKey) {
        return queries.getOptionalPlatform(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
    }

    public void updatePlatform(Platform.Key platformKey, Platform platform, boolean copyProperties, User user) {
        if (!queries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platform.getKey());
        }
        commands.updatePlatform(platformKey, platform, copyProperties, user);
    }

    public void deletePlatform(Platform.Key platformKey, User user) {
        if (!queries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }
        commands.deletePlatform(platformKey, user);
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

    public List<AbstractValuedPropertyView> getProperties(final Platform.Key platformKey, final String path, final User user) {
        if (!queries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }
        if (ROOT_PATH.equals(path)) {
            final List<ValuedPropertyView> globalProperties = queries.getGlobalProperties(platformKey, path, user);
            return new ArrayList<>(globalProperties);
        }
        if (StringUtils.isNotEmpty(path) && path.length() > 1) {
            final Module.Key moduleKey = Module.Key.fromPath(path);
            if (!moduleQueries.moduleExists(moduleKey)) {
                throw new ModuleNotFoundException(moduleKey);
            }
            return queries.getDeployedModuleProperties(platformKey, path, user);
        }
        return Collections.emptyList();
    }

    public Optional<InstanceModelView> getInstanceModel(final Platform.Key platformKey, final String modulePath, final User user) {
        if (!queries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }
        return queries.getInstanceModel(platformKey, modulePath, user);
    }

    public List<AbstractValuedPropertyView> saveProperties(final Platform.Key platformKey,
                                                           final String path,
                                                           final Long platformVersionId,
                                                           final List<AbstractValuedProperty> abstractValuedProperties,
                                                           final User user) {
        if (!queries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }
        if (ROOT_PATH.equals(path)) {
            List<ValuedProperty> valuedProperties = AbstractValuedProperty.filterAbstractValuedPropertyWithType(abstractValuedProperties, ValuedProperty.class);
            // Platform properties are global and should always be of type ValuedProperty
            if (valuedProperties.size() != abstractValuedProperties.size()) {
                throw new IllegalArgumentException("Global properties should always be valued properties");
            }
            commands.savePlatformProperties(platformKey, platformVersionId, valuedProperties, user);
        } else {
            final Module.Key moduleKey = Module.Key.fromPath(path);
            if (!moduleQueries.moduleExists(moduleKey)) {
                throw new ModuleNotFoundException(moduleKey);
            }
            commands.saveModulePropertiesInPlatform(platformKey, path, platformVersionId, abstractValuedProperties, user);
        }

        return getProperties(platformKey, path, user);
    }

    public Map<String, Set<GlobalPropertyUsageView>> getGlobalPropertiesUsage(final Platform.Key platformKey) {

        final Optional<PlatformView> optionalPlatformView = queries.getOptionalPlatform(platformKey);

        if (optionalPlatformView.isPresent()) {

            // Loop on all global properties to get usage
            final PlatformView platformView = optionalPlatformView.get();
            return platformView.getValuedProperties().stream()
                    .collect(Collectors.toMap(ValuedPropertyView::getName, globalProperty -> getGlobalPropertyUsage(platformView, globalProperty)));
        }
        throw new PlatformNotFoundException(platformKey);
    }

    private Set<GlobalPropertyUsageView> getGlobalPropertyUsage(final PlatformView platformView, final ValuedPropertyView globalProperty) {
        final Set<GlobalPropertyUsageView> globalPropertyUsageViews = new HashSet<>();

        // Loop on all deployed modules
        platformView.getDeployedModules().forEach(deployedModule -> {

            // Create the current module key
            TemplateContainer.Key moduleKey = new Module.Key(deployedModule.getName(), deployedModule.getVersion(),
                    deployedModule.isWorkingCopy() ? TemplateContainer.VersionType.workingcopy : TemplateContainer.VersionType.release);

            // Get module properties
            final List<AbstractPropertyView> moduleProperties = new ArrayList<>();
            final boolean isModuleExists = moduleQueries.moduleExists(moduleKey);

            if (isModuleExists) {
               moduleProperties.addAll(AbstractPropertyView.flattenAbstractPropertyView(moduleQueries.getProperties(moduleKey)));

                // Get global variable usage in module properties
                globalPropertyUsageViews.addAll(getModuleGlobalPropertyUsage(globalProperty, moduleProperties));
            }

            // get global variable usage in deployed module properties
            globalPropertyUsageViews.addAll(getDeployedModuleGLobalPropertyUsage(globalProperty, deployedModule, moduleProperties, isModuleExists));
        });
        return globalPropertyUsageViews;
    }

    private List<GlobalPropertyUsageView> getModuleGlobalPropertyUsage(final ValuedPropertyView globalProperty, final List<AbstractPropertyView> moduleProperties) {
        return moduleProperties.stream()
                .filter(moduleProperty -> globalProperty.getName().equals(moduleProperty.getName()))
                .map(moduleProperty -> new GlobalPropertyUsageView(true, ROOT_PATH))
                .collect(Collectors.toList());
    }

    private List<GlobalPropertyUsageView> getDeployedModuleGLobalPropertyUsage(final ValuedPropertyView globalProperty, final DeployedModuleView deployedModule, final List<AbstractPropertyView> moduleProperties, boolean isModuleExists) {
        return AbstractValuedPropertyView.flattenAbstractValuedPropertyView(deployedModule.getValuedProperties()).stream()
                .map(ValuedPropertyView.class::cast)
                .filter(deployedModuleProperty -> deployedModuleProperty.getValue().contains("{{" +  globalProperty.getName() + "}}"))
                .map(deployedModuleProperty -> new GlobalPropertyUsageView(
                        isValuedPropertyInModel(deployedModuleProperty, moduleProperties, isModuleExists), deployedModule.getPropertiesPath()))
                .collect(Collectors.toList());
    }

    private boolean isValuedPropertyInModel(final ValuedPropertyView deployedModuleProperty,
                                            final List<AbstractPropertyView> moduleProperties, final boolean isModuleExists) {
        if (isModuleExists) {
            return moduleProperties.stream()
                    .anyMatch(moduleProperty -> deployedModuleProperty.getName().equals(moduleProperty.getName()));
        }
        return false;
    }
}
