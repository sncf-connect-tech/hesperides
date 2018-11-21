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

import com.github.mustachejava.Code;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.codes.ValueCode;
import org.hesperides.core.domain.files.InstanceFileView;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
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
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.entities.AbstractProperty;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class FileUseCases {

    private final PlatformQueries platformQueries;
    private final ModuleQueries moduleQueries;

    @Autowired
    public FileUseCases(PlatformQueries platformQueries, ModuleQueries moduleQueries) {
        this.platformQueries = platformQueries;
        this.moduleQueries = moduleQueries;
    }

    public List<InstanceFileView> getInstanceFiles(
            String applicationName,
            String platformName,
            String modulePath,
            String moduleName,
            String moduleVersion,
            String instanceName,
            boolean isWorkingCopy,
            boolean simulate) {

        List<InstanceFileView> instanceFiles = new ArrayList<>();

        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        if (!platformQueries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }

        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.getVersionType(isWorkingCopy));
        if (!moduleQueries.moduleExists(moduleKey)) {
            throw new ModuleNotFoundException(moduleKey);
        }

        if (!platformQueries.deployedModuleExists(platformKey, moduleKey, modulePath)) {
            throw new DeployedModuleNotFoundException(platformKey, moduleKey, modulePath);
        }

        // Le paramètre simulate doit être à false pour récupérer les fichiers d'instance
        // et à true pour les fichiers de module
        if (!simulate && !platformQueries.instanceExists(platformKey, moduleKey, modulePath, instanceName)) {
            throw new InstanceNotFoundException(platformKey, moduleKey, modulePath, instanceName);
        }

        ModuleView module = moduleQueries.getOptionalModule(moduleKey).get();
        module.getTechnos().forEach(techno -> techno.getTemplates().forEach(template -> instanceFiles.add(
                new InstanceFileView(platformKey, modulePath, moduleKey, instanceName, template, simulate))));
        module.getTemplates().forEach(template -> instanceFiles.add(
                new InstanceFileView(platformKey, modulePath, moduleKey, instanceName, template, simulate)));

        return instanceFiles;
    }

    public String getFile(
            String applicationName,
            String platformName,
            String path,
            String moduleName,
            String moduleVersion,
            String instanceName,
            String fileName,
            boolean isWorkingCopy,
            String templateNamespace,
            boolean simulate) {

        Platform.Key plateformKey = new Platform.Key(applicationName, platformName);
        if (!platformQueries.platformExists(plateformKey)) {
            throw new PlatformNotFoundException(plateformKey);
        }

        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.getVersionType(isWorkingCopy));
        if (!moduleQueries.moduleExists(moduleKey)) {
            throw new ModuleNotFoundException(moduleKey);
        }

        PlatformView platformView = platformQueries.getOptionalPlatform(plateformKey).get();

        Optional<DeployedModuleView> optionalDeployedModule = platformView.getDeployedModule(moduleKey);

        String templateContent = "";
        if (optionalDeployedModule.isPresent()) {

            //Récupérer le template dont le nameSpace est fourni à partir du module
            Optional<TemplateView> template = moduleQueries
                    .getOptionalModule(moduleKey)
                    .get()
                    .getTemplates()
                    .stream()
                    .filter(t -> templateNamespace.equals(t.getNamespace()))
                    .findFirst();


            //Récupérer l'instance dont le est fourni en entrée
            Optional<InstanceView> instanceView = optionalDeployedModule.get()
                    .getInstances()
                    .stream()
                    .filter(i -> instanceName.equals(i.getName()))
                    .findFirst();

            Map<String, String> propertiesKeyValueMap = new HashMap<>();
            List<ValuedPropertyView> valuedProperties;

            if (template.isPresent()) {

                templateContent = template.get().getContent();

                //Récupérer les propriétés du template
                List<AbstractProperty> templateProperties = template.get()
                        .toDomainInstance(optionalDeployedModule.get().getModuleKey()).extractProperties();

                if (instanceView.isPresent()) {
                    // valorisation du template à partir des valeurs de l'instance

                    valuedProperties = instanceView.get().getValuedProperties();

                    associerCleValeurProperty(templateProperties, propertiesKeyValueMap, valuedProperties);

                    templateContent = getValuedTemplateContent(templateContent, propertiesKeyValueMap);
                } else {
                    //valorisation du template à partir des valeurs du module

                    valuedProperties = AbstractValuedPropertyView.flattenValuedProperties(optionalDeployedModule.get().getValuedProperties())
                            .stream()
                            .map(ValuedPropertyView.class::cast)
                            .collect(Collectors.toList());

                    associerCleValeurProperty(templateProperties, propertiesKeyValueMap, valuedProperties);

                    templateContent = getValuedTemplateContent(templateContent, propertiesKeyValueMap);

                }
            }
        }

        return templateContent;
    }

    protected String getValuedTemplateContent(String templateContent, Map<String, String> propertiesMap) {

        Mustache mustacheTemplate = AbstractProperty.getMustacheInstanceFromStringContent(templateContent);
        Map<String, String> mustachePropertiesMap = new HashMap<>(propertiesMap);

        for (Code code : mustacheTemplate.getCodes()) {
            if (code instanceof ValueCode) {
                propertiesMap.forEach((key, value) ->
                {
                    if (!code.getName().equals(key) && code.getName().contains(key)) {
                        //dans l'implémentation du Hashmap, le key modifier est final
                        mustachePropertiesMap.put(code.getName(), mustachePropertiesMap.get(key));
                        mustachePropertiesMap.remove(key);
                    }
                });
            }
        }

        StringWriter esRequest = new StringWriter();
        mustacheTemplate.execute(esRequest, mustachePropertiesMap);
        esRequest.flush();
        return esRequest.toString();


        /*for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {

            int beginIndex = result.indexOf("{{" + entry.getKey()) < 0 ? result.indexOf("{{ " + entry.getKey()) : result.indexOf("{{" + entry.getKey());
            int endIndex = result.indexOf("}}", beginIndex);
            String prop = result.substring(beginIndex, endIndex + 2);


            result = result
                    .replace(prop, entry.getValue());
        }*/
    }

    private void associerCleValeurProperty(List<AbstractProperty> templateProperties, Map<String, String> propertiesKeyValueMap, List<ValuedPropertyView> valuedProperties) {
        templateProperties.forEach(property ->
                propertiesKeyValueMap.put(
                        property.getName(),
                        valuedProperties.stream()
                                .filter(value -> value.getName().equals(property.getName())).findFirst()
                                .get().getValue())
        );
    }
}
