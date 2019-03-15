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
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterablePropertyItemView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.security.User;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.AbstractProperty;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.IterablePropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView.hidePasswordProperties;

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
        boolean shouldHidePasswordProperties = platform.isProductionPlatform() && !user.isProd();
        return valorizeWithModuleAndGlobalAndInstanceProperties(templateContent, platform, modulePath, moduleKey, moduleQueries.getPropertiesModel(moduleKey), instanceName, shouldHidePasswordProperties);
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
                                                                           List<AbstractPropertyView> modulePropertiesModel,
                                                                           String instanceName,
                                                                           boolean shouldHidePasswordProperties) {

        DeployedModuleView deployedModule = platform.getDeployedModule(modulePath, moduleKey)
                .orElseThrow(() -> new ModuleNotFoundException(moduleKey, modulePath));

        List<AbstractValuedPropertyView> valuedProperties = deployedModule.getValuedProperties();
        if (shouldHidePasswordProperties) {
            valuedProperties = hidePasswordProperties(valuedProperties, modulePropertiesModel);
        }
        FileValuationContext valuationContext = new FileValuationContext(platform, deployedModule, instanceName);
        valuedProperties = valuationContext.completeWithContextualProperties(valuedProperties);

        // Prépare les propriétés faisant référence à d'autres propriétés, de manière récursive
        Map<String, List<AbstractPropertyView>> propertyModelsPerName = modulePropertiesModel.stream().collect(groupingBy(AbstractPropertyView::getName));
        List<AbstractValuedPropertyView> preparedProperties = preparePropertiesValues(valuedProperties, propertyModelsPerName, valuationContext);

        Map<String, Object> scopes = propertiesToScopes(preparedProperties, propertyModelsPerName, valuationContext);
        return replaceMustachePropertiesWithValues(input, scopes);
    }

    // TODO: Refactoriser
    private static List<AbstractValuedPropertyView> preparePropertiesValues(List<AbstractValuedPropertyView> valuedProperties,
                                                                            Map<String, List<AbstractPropertyView>> propertyModelsPerName,
                                                                            FileValuationContext valuationContext) {

        List<AbstractValuedPropertyView> preparedProperties = new ArrayList<>();
        Map<String, Object> scopes = propertiesToScopes(valuedProperties, propertyModelsPerName, valuationContext);

        valuedProperties.forEach(property -> {
            if (property instanceof ValuedPropertyView) {
                ValuedPropertyView valuedProperty = (ValuedPropertyView) property;
                if (StringUtils.contains(valuedProperty.getValue(), "{{")) {
                    valuedProperty = valuedProperty.withValue(replaceMustachePropertiesWithValues(valuedProperty.getValue(), scopes));
                }
                preparedProperties.add(valuedProperty);
            } else if (property instanceof IterableValuedPropertyView) {
                IterableValuedPropertyView iterableValuedProperty = (IterableValuedPropertyView) property;
                IterablePropertyView iterablePropertyModel = (IterablePropertyView) propertyModelsPerName.get(property.getName()).get(0);
                List<AbstractPropertyView> iterablePropertiesModel = iterablePropertyModel != null ? iterablePropertyModel.getProperties() : Collections.EMPTY_LIST;

                List<IterablePropertyItemView> items = new ArrayList<>();
                iterableValuedProperty.getIterablePropertyItems().forEach(valuedPropertyItem -> {
                    List<AbstractValuedPropertyView> preparedIterableProperties = preparePropertiesValues(
                            valuationContext.completeWithContextualProperties(valuedPropertyItem.getAbstractValuedPropertyViews()),
                            iterablePropertiesModel.stream().collect(groupingBy(AbstractPropertyView::getName)),
                            valuationContext
                    );
                    IterablePropertyItemView iterablePropertyItemView = new IterablePropertyItemView(valuedPropertyItem.getTitle(), preparedIterableProperties);
                    items.add(iterablePropertyItemView);
                });
                preparedProperties.add(new IterableValuedPropertyView(iterableValuedProperty.getName(), items));
            }
        });

        // Recursion tant qu'on a pas atteind un point stable,
        // c'est à dire que la phase de substitution n'a entrainé aucun nouveau changement de valeur de propriétés
        return valuedProperties.equals(preparedProperties) ? preparedProperties : preparePropertiesValues(preparedProperties, propertyModelsPerName, valuationContext);
    }

    /**
     * Transforme une liste de propriétés `AbstractValuedPropertyView` pouvant contenir des propriétés simples
     * et des propriétés itérables en map de ce type :
     * - nom-propriété-simple => valeur-propriété-simple
     * - nom-propriété-itérable => map (...)
     */
    private static Map<String, Object> propertiesToScopes(List<AbstractValuedPropertyView> valuedProperties,
                                                          Map<String, List<AbstractPropertyView>> propertyModelsPerName,
                                                          FileValuationContext valuationContext) {
        Map<String, Object> scopes = new HashMap<>();
        valuedProperties.forEach(property -> {
            if (property instanceof ValuedPropertyView) {
                ValuedPropertyView valuedProperty = (ValuedPropertyView) property;
                if (propertyModelsPerName.containsKey(property.getName())) {
                    for (AbstractPropertyView abstractPropertyModel : propertyModelsPerName.get(property.getName())) {
                        PropertyView propertyModel = (PropertyView) abstractPropertyModel;
                        scopes.put(propertyModel.getMustacheContent(), figurePropertyValue(valuedProperty, propertyModel));
                    }
                }
            } else if (property instanceof IterableValuedPropertyView) {
                IterableValuedPropertyView iterableValuedProperty = (IterableValuedPropertyView) property;
                IterablePropertyView iterablePropertyModel = (IterablePropertyView) propertyModelsPerName.get(property.getName()).get(0);
                List<AbstractPropertyView> iterablePropertiesModel = iterablePropertyModel != null ? iterablePropertyModel.getProperties() : Collections.EMPTY_LIST;

                List<Map<String, Object>> items = iterableValuedProperty.getIterablePropertyItems()
                        .stream()
                        .map(item -> propertiesToScopes(
                                valuationContext.completeWithContextualProperties(item.getAbstractValuedPropertyViews()),
                                iterablePropertiesModel.stream().collect(groupingBy(AbstractPropertyView::getName)),
                                valuationContext
                        ))
                        .collect(Collectors.toList());

                scopes.put(iterableValuedProperty.getName(), items);
            }
        });
        // cf. #540 & BDD Scenario: get file with instance properties created by a module property that references itself
        valuedProperties.stream()
                .filter(ValuedPropertyView.class::isInstance).map(ValuedPropertyView.class::cast)
                .forEach(valuedProperty -> {
                    if (!scopes.containsKey(valuedProperty.getName())) {
                        // Cas où une valorisation de propriété a été insérée pour la clef "mustacheContent" mais PAS pour le nom exact de la propriété,
                        // et aucune autre propriété n'a été insérée pour ce nom
                        // (ce qui peut arriver lorsque des propriétés d'instance ou globales ont le même nom),
                        // on l'insère donc maintenant:
                        PropertyView propertyModel = propertyModelsPerName.containsKey(valuedProperty.getName()) ? (PropertyView) propertyModelsPerName.get(valuedProperty.getName()).get(0) : null;
                        scopes.put(valuedProperty.getName(), figurePropertyValue(valuedProperty, propertyModel));
                    }
                });
        // Gestion du cas des propriétés iterables où certaines de ses propriétés
        // peuvent ne pas avoir de valorisation mais le modèle a des valeurs par défaut.
        // cf. BDD Scenario: get file with iterable and default values
        propertyModelsPerName.values().stream()
                .map(propertyModels -> propertyModels.get(0))
                .filter(PropertyView.class::isInstance).map(PropertyView.class::cast)
                .forEach(propertyModel -> {
                    if (!scopes.containsKey(propertyModel.getName())) {
                        scopes.put(propertyModel.getName(), propertyModel.getDefaultValue());
                    }
                    if (!scopes.containsKey(propertyModel.getMustacheContent())) {
                        scopes.put(propertyModel.getMustacheContent(), propertyModel.getDefaultValue());
                    }
                });
        return scopes;
    }

    static private String figurePropertyValue(ValuedPropertyView valuedProperty, PropertyView property) {
        // Aucun modèle de propriété n'existe pour les propriétés globales & prédéfinies,
        // donc pour elles `propertyModel` sera null et il n'y a aucune valeur par défaut associée
        String defaultValue = property != null ? property.getDefaultValue() : "";
        // Pour la valeur, si la propriété n'est pas valorisée, on prend la valeur par défaut
        return StringUtils.trim(StringUtils.defaultString(valuedProperty.getValue(), defaultValue));
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
