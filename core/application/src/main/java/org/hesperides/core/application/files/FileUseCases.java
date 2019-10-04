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

import org.hesperides.core.application.platforms.properties.PropertyValuationBuilder;
import org.hesperides.core.domain.files.InstanceFileView;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.visitors.IterablePropertyVisitor;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitorsSequence;
import org.hesperides.core.domain.platforms.entities.properties.visitors.SimplePropertyVisitor;
import org.hesperides.core.domain.platforms.exceptions.InstanceNotFoundException;
import org.hesperides.core.domain.platforms.exceptions.PlatformNotFoundException;
import org.hesperides.core.domain.platforms.queries.PlatformQueries;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hesperides.core.domain.platforms.entities.properties.PropertyType.GLOBAL;
import static org.hesperides.core.domain.platforms.entities.properties.PropertyType.PREDEFINED;

@Component
public class FileUseCases {

    private final PlatformQueries platformQueries;
    private final ModuleQueries moduleQueries;

    @Autowired
    public FileUseCases(PlatformQueries platformQueries, ModuleQueries moduleQueries) {
        this.platformQueries = platformQueries;
        this.moduleQueries = moduleQueries;
    }

    /**
     * Transforme une liste de propriétés `AbstractValuedPropertyView` pouvant contenir des propriétés simples
     * et des propriétés itérables en map de ce type :
     * - nom-propriété-simple => valeur-propriété-simple
     * - nom-propriété-itérable => list (...)
     * @param propertyVisitorsSequence
     */
    public static Map<String, Object> propertiesToScopes(PropertyVisitorsSequence propertyVisitorsSequence) {
        Map<String, Object> scopes = new HashMap<>();
        propertyVisitorsSequence.stream().forEach(propertyVisitor -> {
            if (propertyVisitor instanceof SimplePropertyVisitor) {
                ((SimplePropertyVisitor) propertyVisitor).getMustacheKeyValues().forEach(scopes::put);
            } else {
                IterablePropertyVisitor iterablePropertyVisitor = (IterablePropertyVisitor) propertyVisitor;
                scopes.put(
                        iterablePropertyVisitor.getName(),
                        iterablePropertyVisitor.getItems().stream()
                                .map(FileUseCases::propertiesToScopes)
                                .collect(Collectors.toList()));
            }
        });
        // cf. #540 & BDD Scenario: get file with instance properties created by a module property that references itself
        propertyVisitorsSequence.stream()
                .filter(SimplePropertyVisitor.class::isInstance)
                .map(SimplePropertyVisitor.class::cast)
                .forEach(propertyVisitor -> {
                    Optional<String> optValue = propertyVisitor.getValueOrDefault();
                    if (!scopes.containsKey(propertyVisitor.getName())) {
                        // Cas où une valorisation de propriété a été insérée pour la clef "mustacheContent" mais PAS pour le nom exact de la propriété,
                        // et aucune autre propriété n'a été insérée pour ce nom
                        // (ce qui peut arriver lorsque des propriétés d'instance ou globales ont le même nom),
                        // on l'insère donc maintenant:
                        optValue.ifPresent(value -> scopes.put(propertyVisitor.getName(), value));
                    }
                });
        return scopes;
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
                        getValorizedInstanceFile(template, platform, moduleKey, moduleQueries.getPropertiesModel(moduleKey), instanceName, modulePath, getModuleValuesIfInstanceDoesntExist))
                .collect(Collectors.toList());
    }

    private static InstanceFileView getValorizedInstanceFile(TemplateView template,
                                                             PlatformView platform,
                                                             Module.Key moduleKey,
                                                             List<AbstractPropertyView> modulePropertiesModel,
                                                             String instanceName,
                                                             String modulePath,
                                                             boolean getModuleValuesIfInstanceDoesntExist) {

        String location = valorizeWithModuleAndGlobalAndInstanceProperties(template.getLocation(), platform, modulePath, moduleKey, modulePropertiesModel, instanceName, false);
        String filename = valorizeWithModuleAndGlobalAndInstanceProperties(template.getFilename(), platform, modulePath, moduleKey, modulePropertiesModel, instanceName, false);
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
        boolean shouldHidePasswordProperties = platform.isProductionPlatform() && !user.hasProductionRoleForApplication(applicationName);
        return valorizeWithModuleAndGlobalAndInstanceProperties(templateContent, platform, modulePath, moduleKey, moduleQueries.getPropertiesModel(moduleKey), instanceName, shouldHidePasswordProperties);
    }

    private void validateRequiredEntities(Platform.Key platformKey,
                                          Module.Key moduleKey,
                                          String modulePath,
                                          boolean getModuleValuesIfInstanceDoesntExist,
                                          String instanceName) {

        String propertiesPath = DeployedModule.generatePropertiesPath(moduleKey, modulePath);

        if (!getModuleValuesIfInstanceDoesntExist && !platformQueries.instanceExists(platformKey, propertiesPath, instanceName)) {
            throw new InstanceNotFoundException(platformKey, moduleKey, modulePath, instanceName);
        }
    }

    static String valorizeWithModuleAndGlobalAndInstanceProperties(String input,
                                                                   PlatformView platform,
                                                                   String modulePath,
                                                                   Module.Key moduleKey,
                                                                   List<AbstractPropertyView> modulePropertiesModels,
                                                                   String instanceName,
                                                                   boolean shouldHidePasswordProperties) {

        PropertyVisitorsSequence preparedPropertyVisitors = PropertyValuationBuilder.buildPropertyVisitorsSequence(
                    platform, modulePath, moduleKey, modulePropertiesModels, instanceName, shouldHidePasswordProperties, EnumSet.of(GLOBAL, PREDEFINED))
                .removeMustachesInPropertyValues()
                .passOverPropertyValuesToChildItems();
        Map<String, Object> scopes = propertiesToScopes(preparedPropertyVisitors);
        return PropertyValuationBuilder.replaceMustachePropertiesWithValues(input, scopes);
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
