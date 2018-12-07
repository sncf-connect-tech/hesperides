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
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
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
        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.getVersionType(isWorkingCopy));
        validateExistingData(modulePath, instanceName, simulate, platformKey, moduleKey);

        PlatformView platform = platformQueries.getOptionalPlatform(platformKey).get();
        List<AbstractValuedPropertyView> moduleValuedProperties = platform.getDeployedModule(moduleKey).get().getValuedProperties();
        List<AbstractValuedPropertyView> globalAndModuleProperties = Stream.concat(platform.getGlobalProperties().stream(), moduleValuedProperties.stream()).collect(Collectors.toList());

        ModuleView module = moduleQueries.getOptionalModule(moduleKey).get();
        // D'abord les templates des technos
        module.getTechnos().forEach(techno -> techno.getTemplates().forEach(template -> {
            setLocationAndFilenameAndAddInstance(modulePath, instanceName, simulate, instanceFiles, platformKey, moduleKey, globalAndModuleProperties, template);
        }));
        // Puis ceux des modules
        module.getTemplates().forEach(template -> {
            setLocationAndFilenameAndAddInstance(modulePath, instanceName, simulate, instanceFiles, platformKey, moduleKey, globalAndModuleProperties, template);
        });

        return instanceFiles;
    }

    private void setLocationAndFilenameAndAddInstance(String modulePath, String instanceName, boolean simulate, List<InstanceFileView> instanceFiles, Platform.Key platformKey, Module.Key moduleKey, List<AbstractValuedPropertyView> globalAndModuleProperties, TemplateView template) {
        String location = valueTemplateProperties(template.getLocation(), globalAndModuleProperties);
        String filename = valueTemplateProperties(template.getFilename(), globalAndModuleProperties);
        instanceFiles.add(new InstanceFileView(location, filename, platformKey, modulePath, moduleKey, instanceName, template, simulate));
    }

    private void validateExistingData(String modulePath, String instanceName, boolean simulate, Platform.Key platformKey, Module.Key moduleKey) {

        if (!platformQueries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }

        if (!moduleQueries.moduleExists(moduleKey)) {
            throw new ModuleNotFoundException(moduleKey);
        }

        if (!platformQueries.deployedModuleExists(platformKey, moduleKey, modulePath)) {
            throw new DeployedModuleNotFoundException(platformKey, moduleKey, modulePath);
        }

        // Le paramètre `simulate` doit être à `false` pour récupérer
        // les fichiers d'instance et à `true` pour les fichiers de module.
        if (!simulate && !platformQueries.instanceExists(platformKey, moduleKey, modulePath, instanceName)) {
            throw new InstanceNotFoundException(platformKey, moduleKey, modulePath, instanceName);
        }
    }

    public String getFile(
            String applicationName,
            String platformName,
            String modulePath,
            String moduleName,
            String moduleVersion,
            String instanceName,
            String templateName,
            Boolean isWorkingCopy,
            String templateNamespace,
            boolean simulate) {

        // Contrôler l'existence des données
        // Récupérer le template
        // Valoriser (globales, modules et instances)

        // On va avoir besoin du template, donc on vérifie que le module ou la techno et le template existent bien.
        // Ensuite on a besoin de la valorisation au niveau de la plateforme, du module et de l'instance
        // donc on vérifie l'existence de la plateforme, du module et de l'instance.

        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.getVersionType(isWorkingCopy));
        validateExistingData(modulePath, instanceName, simulate, platformKey, moduleKey);

        ModuleView module = moduleQueries.getOptionalModule(moduleKey).get();
        Optional<TemplateView> template = getTemplate(module, templateName, templateNamespace);
        // Récupère le contenu du template après avoir vérifié que le template existe bien
        String templateContent = template.orElseThrow(() -> new TemplateNotFoundException(moduleKey, templateName)).getContent();

        PlatformView platform = platformQueries.getOptionalPlatform(platformKey).get();
        List<AbstractValuedPropertyView> moduleValuedProperties = platform.getDeployedModule(moduleKey).get().getValuedProperties();

        return valueTemplateProperties(templateContent, moduleValuedProperties);


        // Propriétés itérables ?

        // Remplacer les valorisation entre moustaches
        // soit par la valeur d'une propriété globale
        // soit par la valeur d'une propriété d'instance (si on est au niveau de l'instance)

//        return replacePropertiesWithValues(templateContent, globalAndModuleProperties);
    }

    private String valueTemplateProperties(String templateContent, List<AbstractValuedPropertyView> valuedProperties) {
        Map<String, Object> scopes = propertiesToScopes(valuedProperties);

        Mustache mustache = AbstractProperty.getMustacheInstanceFromStringContent(templateContent);
        StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, scopes);
        stringWriter.flush();

        return stringWriter.toString();
    }

    private Map<String, Object> propertiesToScopes(List<AbstractValuedPropertyView> properties) {
        Map<String, Object> scopes = new HashMap<>();
        properties.forEach(property -> {
            if (property instanceof ValuedPropertyView) {

                ValuedPropertyView valuedProperty = (ValuedPropertyView) property;
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

    /**
     * Renvoie un template s'il existe dans les templates du module
     * ou dans les templates des technos du module.
     * On vérifie son existence à partir de son nom et son namespace.
     */
    private Optional<TemplateView> getTemplate(ModuleView module, String templateName, String templateNamespace) {
        Stream<TemplateView> moduleTechnosTemplates = module.getTechnos().stream().flatMap(technoView -> technoView.getTemplates().stream());
        Stream<TemplateView> allTemplates = Stream.concat(module.getTemplates().stream(), moduleTechnosTemplates);

        return allTemplates.filter(templateView ->
                templateView.getName().equalsIgnoreCase(templateName) && templateView.getNamespace().equalsIgnoreCase(templateNamespace))
                .findFirst();
    }
}
