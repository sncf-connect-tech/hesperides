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
import org.hesperides.core.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.exceptions.DeployedModuleNotFoundException;
import org.hesperides.core.domain.platforms.exceptions.InstanceNotFoundException;
import org.hesperides.core.domain.platforms.exceptions.PlatformNotFoundException;
import org.hesperides.core.domain.platforms.queries.PlatformQueries;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
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
        Map<String, String> globalAndModuleProperties = getGlobalAndModuleProperties(moduleKey, platform);

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

    private void setLocationAndFilenameAndAddInstance(String modulePath, String instanceName, boolean simulate, List<InstanceFileView> instanceFiles, Platform.Key platformKey, Module.Key moduleKey, Map<String, String> globalAndModuleProperties, TemplateView template) {
        String location = replacePropertiesWithValues(template.getLocation(), globalAndModuleProperties);
        String filename = replacePropertiesWithValues(template.getFilename(), globalAndModuleProperties);
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
        Map<String, String> globalAndModuleProperties = getGlobalAndModuleProperties(moduleKey, platform);


        // Propriétés itérables ?

        // Remplacer les valorisation entre moustaches
        // soit par la valeur d'une propriété globale
        // soit par la valeur d'une propriété d'instance (si on est au niveau de l'instance)

        return replacePropertiesWithValues(templateContent, globalAndModuleProperties);
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

    /**
     * Récupère la liste des propriétés globales
     * et des propriétés simples de module
     * sous forme de clé-valeur.
     */
    private Map<String, String> getGlobalAndModuleProperties(Module.Key moduleKey, PlatformView platform) {
        Stream<ValuedPropertyView> moduleSimpleProperties = platform.getDeployedModule(moduleKey).get().getValuedProperties()
                .stream()
                .filter(ValuedPropertyView.class::isInstance)
                .map(ValuedPropertyView.class::cast);

        return Stream.concat(platform.getGlobalProperties().stream(), moduleSimpleProperties)
                .collect(Collectors.toMap(ValuedPropertyView::getName, ValuedPropertyView::getValue));
    }


    private String replacePropertiesWithValues(String templateContent, Map<String, String> valuedProperties) {

        Mustache mustache = AbstractProperty.getMustacheInstanceFromStringContent(templateContent);
        Map<String, String> mustacheValuedProperties = new HashMap<>();

        for (Code code : mustache.getCodes()) {
            if (code instanceof ValueCode) {
                valuedProperties.forEach((propertyName, propertyValue) -> {
                    if (code.getName().startsWith(propertyName)) {
                        mustacheValuedProperties.put(code.getName(), valuedProperties.get(propertyName));
                    }
                });
            }
        }

        StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, mustacheValuedProperties);
        stringWriter.flush();

        return stringWriter.toString();
    }
}
