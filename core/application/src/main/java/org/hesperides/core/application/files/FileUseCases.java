/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.core.application.files;

import com.github.mustachejava.Mustache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.files.InstanceFileView;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.exceptions.DeployedModuleNotFoundException;
import org.hesperides.core.domain.platforms.exceptions.InstanceNotFoundException;
import org.hesperides.core.domain.platforms.exceptions.PlatformNotFoundException;
import org.hesperides.core.domain.platforms.queries.PlatformQueries;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.platforms.queries.views.InstanceView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterablePropertyItemView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.security.User;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.AbstractProperty;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView.hidePasswordProperties;

@Component
@Slf4j
public class FileUseCases {

    private final PlatformQueries platformQueries;
    private final ModuleQueries moduleQueries;

    @Autowired
    public FileUseCases(PlatformQueries platformQueries, ModuleQueries moduleQueries) {
        this.platformQueries = platformQueries;
        this.moduleQueries = moduleQueries;
    }

    public List<InstanceFileView> getFiles(
            String applicationName,
            String platformName,
            String modulePath,
            String moduleName,
            String moduleVersion,
            String instanceName,
            boolean isWorkingCopy,
            boolean getModuleValuesIfInstanceDoesntExist) {

        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.getVersionType(isWorkingCopy));
        validateRequiredEntities(platformKey, moduleKey, modulePath, getModuleValuesIfInstanceDoesntExist, instanceName);

        PlatformView platform = platformQueries.getOptionalPlatform(platformKey).orElseThrow(() -> new PlatformNotFoundException(platformKey));
        ModuleView module = moduleQueries.getOptionalModule(moduleKey).orElseThrow(() -> new ModuleNotFoundException(moduleKey));

        List<TemplateView> technosAndModuleTemplates = Stream.concat(
                module.getTechnos().stream().map(TechnoView::getTemplates).flatMap(List::stream),
                module.getTemplates().stream())
                .collect(Collectors.toList());

        return technosAndModuleTemplates.stream()
                .map(template ->
                        getValorizedInstanceFile(template, platform, moduleKey, instanceName, modulePath, getModuleValuesIfInstanceDoesntExist))
                .collect(Collectors.toList());
    }

    private static InstanceFileView getValorizedInstanceFile(TemplateView template,
                                                             PlatformView platform,
                                                             Module.Key moduleKey,
                                                             String instanceName,
                                                             String modulePath,
                                                             boolean getModuleValuesIfInstanceDoesntExist) {

        String location = valorizeWithModuleAndGlobalAndInstanceProperties(template.getLocation(), platform, modulePath, moduleKey, instanceName, false);
        String filename = valorizeWithModuleAndGlobalAndInstanceProperties(template.getFilename(), platform, modulePath, moduleKey, instanceName, false);
        return new InstanceFileView(location, filename, platform, modulePath, moduleKey, instanceName, template, getModuleValuesIfInstanceDoesntExist);
    }

    public String getFile(
            String applicationName,
            String platformName,
            String modulePath,
            String moduleName,
            String moduleVersion,
            String instanceName,
            String templateName,
            boolean isWorkingCopy,
            String templateNamespace,
            boolean getModuleValuesIfInstanceDoesntExist,
            User user) {

        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.getVersionType(isWorkingCopy));
        validateRequiredEntities(platformKey, moduleKey, modulePath, getModuleValuesIfInstanceDoesntExist, instanceName);

        ModuleView module = moduleQueries.getOptionalModule(moduleKey).orElseThrow(() -> new ModuleNotFoundException(moduleKey));
        Optional<TemplateView> template = getTemplate(module, templateName, templateNamespace);
        String templateContent = template.orElseThrow(() -> new TemplateNotFoundException(moduleKey, templateName)).getContent();

        PlatformView platform = platformQueries.getOptionalPlatform(platformKey).orElseThrow(() -> new PlatformNotFoundException(platformKey));
        boolean shouldHidePasswordProperties = platform.isProductionPlatform() && !user.isProd();
        return valorizeWithModuleAndGlobalAndInstanceProperties(templateContent, platform, modulePath, moduleKey, instanceName, shouldHidePasswordProperties);
    }

    private void validateRequiredEntities(Platform.Key platformKey,
                                          Module.Key moduleKey,
                                          String modulePath,
                                          boolean getModuleValuesIfInstanceDoesntExist,
                                          String instanceName) {

        if (!platformQueries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }
        if (!moduleQueries.moduleExists(moduleKey)) {
            throw new ModuleNotFoundException(moduleKey);
        }
        if (!platformQueries.deployedModuleExists(platformKey, moduleKey, modulePath)) {
            throw new DeployedModuleNotFoundException(platformKey, moduleKey, modulePath);
        }
        if (!getModuleValuesIfInstanceDoesntExist && !platformQueries.instanceExists(platformKey, moduleKey, modulePath, instanceName)) {
            throw new InstanceNotFoundException(platformKey, moduleKey, modulePath, instanceName);
        }
    }

    private static String valorizeWithModuleAndGlobalAndInstanceProperties(String input,
                                                                           PlatformView platform,
                                                                           String modulePath,
                                                                           Module.Key moduleKey,
                                                                           String instanceName,
                                                                           boolean shouldHidePasswordProperties) {

        DeployedModuleView deployedModule = platform.getDeployedModule(modulePath, moduleKey)
                .orElseThrow(() -> new ModuleNotFoundException(moduleKey, modulePath));

        List<AbstractValuedPropertyView> moduleProperties = deployedModule.getValuedProperties();
        if (shouldHidePasswordProperties) {
            moduleProperties = hidePasswordProperties(moduleProperties);
        }
        List<ValuedPropertyView> globalProperties = platform.getGlobalProperties();
        List<ValuedPropertyView> instanceProperties = getInstanceProperties(deployedModule.getInstances(), deployedModule.getInstancesModel(), instanceName);
        List<ValuedPropertyView> predefinedProperties = getPredefinedProperties(platform, deployedModule, instanceName);

        // Concatène les propriétés globales, de module, d'instance et prédéfinies
        List<AbstractValuedPropertyView> moduleAndGlobalProperties = concat(moduleProperties, globalProperties, platform, moduleKey, "global one during 1st pass");
        List<AbstractValuedPropertyView> moduleGlobalAndInstanceProperties = concat(moduleAndGlobalProperties, instanceProperties, platform, moduleKey, "instance one during 2nd pass");
        List<AbstractValuedPropertyView> moduleGlobalInstanceAndPredefinedProperties = concat(moduleGlobalAndInstanceProperties, predefinedProperties, platform, moduleKey, "predefined one during 3rd pass");

        // Prépare les propriétés faisant référence à d'autres propriétés, de manière récursive
        List<AbstractValuedPropertyView> preparedProperties = preparePropertiesValues(moduleGlobalInstanceAndPredefinedProperties, moduleGlobalInstanceAndPredefinedProperties);

        Map<String, Object> scopes = propertiesToScopes(preparedProperties);
        return replaceMustachePropertiesWithValues(input, scopes);
    }

    // TODO Refactoriser
    private static List<AbstractValuedPropertyView> preparePropertiesValues(List<? extends AbstractValuedPropertyView> propertiesToValorise,
                                                                            List<? extends AbstractValuedPropertyView> propertiesValues) {

        List<AbstractValuedPropertyView> preparedProperties = new ArrayList<>();
        Map<String, Object> scopes = propertiesToScopes(propertiesValues);

        propertiesToValorise.forEach(property -> {
            if (property instanceof ValuedPropertyView) {

                ValuedPropertyView valuedProperty = (ValuedPropertyView) property;
                if (StringUtils.contains(valuedProperty.getValue(), "{{")) {
                    String newValue = replaceMustachePropertiesWithValues(valuedProperty.getValue(), scopes);
                    preparedProperties.add(valuedProperty.withValue(newValue));
                } else {
                    preparedProperties.add(valuedProperty);
                }


            } else if (property instanceof IterableValuedPropertyView) {
                IterableValuedPropertyView iterableValuedProperty = (IterableValuedPropertyView) property;

                List<IterablePropertyItemView> items = new ArrayList<>();
                iterableValuedProperty.getIterablePropertyItems().forEach(iterablePropertyItem -> {
                    IterablePropertyItemView iterablePropertyItemView = new IterablePropertyItemView(iterablePropertyItem.getTitle(), preparePropertiesValues(iterablePropertyItem.getAbstractValuedPropertyViews(), propertiesValues));
                    items.add(iterablePropertyItemView);
                });
                preparedProperties.add(new IterableValuedPropertyView(iterableValuedProperty.getName(), items));
            }
        });

        // TODO Commenter
        return propertiesToValorise.equals(preparedProperties) ? preparedProperties : preparePropertiesValues(preparedProperties, propertiesValues);
    }

    /**
     * Transforme une liste de propriétés `AbstractValuedPropertyView` pouvant contenir des propriétés simples
     * et des propriétés itérables en map de ce type :
     * - nom-propriété-simple => valeur-propriété-simple
     * - nom-propriété-itérable => map (...)
     */
    private static Map<String, Object> propertiesToScopes(List<? extends AbstractValuedPropertyView> properties) {
        Map<String, Object> scopes = new HashMap<>();
        properties.forEach(property -> {
            if (property instanceof ValuedPropertyView) {

                ValuedPropertyView valuedProperty = (ValuedPropertyView) property;
                // Pour la valeur, si la propriété n'est pas valorisée, on prend la valeur par défaut
                scopes.put(valuedProperty.getMustacheContentOrName(), StringUtils.trim(StringUtils.defaultString(valuedProperty.getValue(), valuedProperty.getDefaultValue())));

            } else if (property instanceof IterableValuedPropertyView) {
                IterableValuedPropertyView iterableValuedProperty = (IterableValuedPropertyView) property;

                List<Map<String, Object>> items = iterableValuedProperty.getIterablePropertyItems()
                        .stream()
                        .map(item -> propertiesToScopes(item.getAbstractValuedPropertyViews()))
                        .collect(Collectors.toList());

                scopes.put(iterableValuedProperty.getName(), items);
            }
        });
        return scopes;
    }

    /**
     * Remplace les propriétés entre moustaches par leur valorisation
     * à l'aide du framework Mustache.
     */
    private static String replaceMustachePropertiesWithValues(String input, Map<String, Object> scopes) {
        Mustache mustache = AbstractProperty.getMustacheInstanceFromStringContent(input);
        StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, scopes);
        stringWriter.flush();
        return stringWriter.toString();
    }

    private static List<ValuedPropertyView> getPredefinedProperties(PlatformView platform, DeployedModuleView deployedModule, String instanceName) {
        List<ValuedPropertyView> predefinedProperties = new ArrayList<>();
        predefinedProperties.add(new ValuedPropertyView("hesperides.application.name", platform.getApplicationName()));
        predefinedProperties.add(new ValuedPropertyView("hesperides.application.version", platform.getVersion()));
        predefinedProperties.add(new ValuedPropertyView("hesperides.platform.name", platform.getPlatformName()));
        predefinedProperties.add(new ValuedPropertyView("hesperides.module.name", deployedModule.getName()));
        predefinedProperties.add(new ValuedPropertyView("hesperides.module.version", deployedModule.getVersion()));
        String modulePath = deployedModule.getModulePath();
        predefinedProperties.add(new ValuedPropertyView("hesperides.module.path.full", modulePath.replace('#', '/')));
        predefinedProperties.addAll(getPathLogicalGroups(modulePath));
        predefinedProperties.add(new ValuedPropertyView("hesperides.instance.name", instanceName));
        return predefinedProperties;
    }

    private static List<ValuedPropertyView> getPathLogicalGroups(String modulePath) {
        List<ValuedPropertyView> pathLogicalGroups = new ArrayList<>();
        String[] groups = modulePath.split("#");
        for (int index = 1; index < groups.length; index++) {
            pathLogicalGroups.add(new ValuedPropertyView("hesperides.module.path." + (index - 1), groups[index]));
        }
        return pathLogicalGroups;
    }

    private static List<ValuedPropertyView> getInstanceProperties(List<InstanceView> instances, List<String> instancesModel, String instanceName) {
        return instances.stream()
                .filter(instance -> instance.getName().equalsIgnoreCase(instanceName))
                .findFirst()
                .map(InstanceView::getValuedProperties)
                .orElse(Collections.emptyList())
                .stream()
                // #539 La propriété d'instance doit faire partie du model d'instances
                .filter(instanceProperty -> instancesModel.contains(instanceProperty.getName()))
                .collect(Collectors.toList());
    }

    private static List<AbstractValuedPropertyView> concat(List<? extends AbstractValuedPropertyView> listOfProps1,
                                                           List<? extends AbstractValuedPropertyView> listOfProps2,
                                                           PlatformView platform,
                                                           Module.Key moduleKey,
                                                           String overridingPropIdForWarning) {
        List<AbstractValuedPropertyView> properties = new ArrayList<>(listOfProps2);
        Set<String> replacableStrings = properties.stream()
                .map(AbstractValuedPropertyView::getMustacheContentOrName)
                .collect(Collectors.toSet());
        for (AbstractValuedPropertyView property : listOfProps1) {
            if (replacableStrings.contains(property.getMustacheContentOrName())) {
                log.debug("{}-{} {}: During valorization, property {} was overriden by {} with same name",
                        platform.getApplicationName(), platform.getPlatformName(), moduleKey.getNamespaceWithoutPrefix(),
                        property.getName(), overridingPropIdForWarning);
            } else {
                properties.add(property);
            }
        }
        return properties;
    }

    /**
     * Renvoie un template s'il existe dans le module ou les technos du module.
     * On vérifie son existence à partir de son nom et son namespace.
     */
    private static Optional<TemplateView> getTemplate(ModuleView module, String templateName, String templateNamespace) {
        Stream<TemplateView> moduleTechnosTemplates = module.getTechnos().stream().flatMap(technoView -> technoView.getTemplates().stream());
        Stream<TemplateView> allTemplates = Stream.concat(module.getTemplates().stream(), moduleTechnosTemplates);

        return allTemplates.filter(templateView ->
                templateView.getName().equalsIgnoreCase(templateName) && templateView.getNamespace().equalsIgnoreCase(templateNamespace))
                .findFirst();
    }
}
