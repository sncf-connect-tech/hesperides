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
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
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

@Component
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

        PlatformView platform = platformQueries.getOptionalPlatform(platformKey).get();
        ModuleView module = moduleQueries.getOptionalModule(moduleKey).get();

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

        String location = valorizeWithModuleAndGlobalAndInstanceProperties(template.getLocation(), platform, moduleKey, instanceName);
        String filename = valorizeWithModuleAndGlobalAndInstanceProperties(template.getFilename(), platform, moduleKey, instanceName);
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
            boolean getModuleValuesIfInstanceDoesntExist) {

        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.getVersionType(isWorkingCopy));
        validateRequiredEntities(platformKey, moduleKey, modulePath, getModuleValuesIfInstanceDoesntExist, instanceName);

        ModuleView module = moduleQueries.getOptionalModule(moduleKey).get();
        Optional<TemplateView> template = getTemplate(module, templateName, templateNamespace);
        String templateContent = template.orElseThrow(() -> new TemplateNotFoundException(moduleKey, templateName)).getContent();

        PlatformView platform = platformQueries.getOptionalPlatform(platformKey).get();
        return valorizeWithModuleAndGlobalAndInstanceProperties(templateContent, platform, moduleKey, instanceName);
    }

    private void validateRequiredEntities(Platform.Key platformKey, Module.Key moduleKey, String modulePath, boolean getModuleValuesIfInstanceDoesntExist, String instanceName) {

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

    /**
     * 1. Une propriété peut être valorisée au niveau de la platforme (propriété globale)
     * et au niveau du module (valorisation classique).
     * <p>
     * 2. La valorisation d'une propriété au niveau du module peut faire référence
     * à une propriété globale ou d'instance.
     * <p>
     * 3. La valorisation d'une propriété d'instance peut faire référence
     * à une propriété globale.
     */
    private static String valorizeWithModuleAndGlobalAndInstanceProperties(String input, PlatformView platform, Module.Key moduleKey, String instanceName) {
        DeployedModuleView deployedModule = platform.getDeployedModule(moduleKey).get();

        List<AbstractValuedPropertyView> moduleProperties = deployedModule.getValuedProperties();
        List<ValuedPropertyView> globalProperties = platform.getGlobalProperties();
        List<ValuedPropertyView> instanceProperties = getInstanceProperties(deployedModule.getInstances(), instanceName);

        input = replaceMustachePropertiesWithValues(input, concat(moduleProperties, globalProperties)); // 1.*
        input = replaceMustachePropertiesWithValues(input, concat(instanceProperties, globalProperties));// 2.*
        return replaceMustachePropertiesWithValues(input, globalProperties); // 3.*
    }

    /**
     * Remplace les propriétés entre moustaches par leur valorisation
     * à l'aide du framework Mustache.
     */
    private static String replaceMustachePropertiesWithValues(String input, List<? extends AbstractValuedPropertyView> properties) {
        Map<String, Object> scopes = propertiesToScopes(properties);

        Mustache mustache = AbstractProperty.getMustacheInstanceFromStringContent(input);
        StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, scopes);
        stringWriter.flush();

        return stringWriter.toString();
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
                // Si on n'a pas la valeur entre moustaches, c'est le cas pour les propriétés globales
                // et les propriétés d'instance, on prend le nom de la propriété
                String propertyToReplace = StringUtils.defaultString(valuedProperty.getMustacheContent(), valuedProperty.getName()).trim();
                scopes.put(propertyToReplace, valuedProperty.getValue().trim());

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

    private static List<ValuedPropertyView> getInstanceProperties(List<InstanceView> instances, String instanceName) {
        return instances.stream()
                .filter(instance -> instance.getName().equalsIgnoreCase(instanceName))
                .findFirst()
                .map(InstanceView::getValuedProperties)
                .orElse(Collections.emptyList());
    }

    private static List<AbstractValuedPropertyView> concat(List<? extends AbstractValuedPropertyView> properties, List<ValuedPropertyView> otherProperties) {
        return Stream
                .concat(properties.stream(), otherProperties.stream())
                .collect(Collectors.toList());
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
