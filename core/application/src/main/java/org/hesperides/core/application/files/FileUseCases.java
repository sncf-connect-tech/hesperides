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
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.AbstractProperty;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView.hidePasswordProperties;

@Component
public class FileUseCases {

    private static int MAX_PREPARE_PROPERTIES_COUNT = 10;
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
        boolean shouldHidePasswordProperties = platform.isProductionPlatform() && !user.hasProductionRoleForApplication(applicationName);
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

    // public for testing
    public static String valorizeWithModuleAndGlobalAndInstanceProperties(String input,
                                                                          PlatformView platform,
                                                                          String modulePath,
                                                                          Module.Key moduleKey,
                                                                          List<AbstractPropertyView> modulePropertiesModels,
                                                                          String instanceName,
                                                                          boolean shouldHidePasswordProperties) {

        DeployedModuleView deployedModule = platform.getDeployedModule(modulePath, moduleKey)
                .orElseThrow(() -> new ModuleNotFoundException(moduleKey, modulePath));

        List<AbstractValuedPropertyView> valuedProperties = deployedModule.getValuedProperties();
        if (shouldHidePasswordProperties) {
            valuedProperties = hidePasswordProperties(valuedProperties, modulePropertiesModels);
        }
        PropertyVisitorsSequence propertyVisitors = PropertyVisitorsSequence.fromModelAndValuedProperties(modulePropertiesModels, valuedProperties);
        List<AbstractValuedPropertyView> extraValuedPropertiesWithoutModel = extractValuedPropertiesWithoutModel(valuedProperties, propertyVisitors);
        // A ce stade, `valuedProperties` ne contient plus que les valorisations de propriétés sans modèle associé
        FileValuationContext valuationContext = new FileValuationContext(platform, deployedModule, instanceName, extraValuedPropertiesWithoutModel);
        PropertyVisitorsSequence completedPropertyVisitors = valuationContext.completeWithContextualProperties(propertyVisitors);
        // Prépare les propriétés faisant référence à d'autres propriétés, de manière récursive :
        PropertyVisitorsSequence preparedPropertyVisitors = preparePropertiesValues(completedPropertyVisitors, valuationContext, 0);
        Map<String, Object> scopes = propertiesToScopes(removeMustachesInPropertyValues(preparedPropertyVisitors));
        return replaceMustachePropertiesWithValues(input, scopes);
    }

    private static List<AbstractValuedPropertyView> extractValuedPropertiesWithoutModel(List<AbstractValuedPropertyView> valuedProperties,
                                                                                        PropertyVisitorsSequence propertyVisitors) {
        Set<String> propertyWithModelNames = propertyVisitors.getProperties().stream()
                .map(PropertyVisitor::getName)
                .collect(Collectors.toSet());
        return valuedProperties.stream()
                .filter(vp -> !propertyWithModelNames.contains(vp.getName()))
                .collect(Collectors.toList());
    }

    private static PropertyVisitorsSequence preparePropertiesValues(PropertyVisitorsSequence propertyVisitors,
                                                                    FileValuationContext valuationContext,
                                                                    int iterationCount) {
        if (iterationCount > MAX_PREPARE_PROPERTIES_COUNT) {
            throw new InfiniteMustacheRecursion("Infinite loop due to self-referencing property or template");
        }
        PropertyVisitorsSequence preparedPropertyVisitors = propertyVisitors.mapSimplesRecursive(propertyVisitor -> {
            Optional<String> optValue = propertyVisitor.getValue();
            if (optValue.isPresent() && StringUtils.contains(optValue.get(), "}}")) { // not bullet-proof but a false positive on mustaches escaped by a delimiter set is OK
                // iso-legacy: on inclue les valorisations sans modèle ici
                // cf. BDD Scenario: get file with property valorized with another valued property
                Map<String, Object> scopes = propertiesToScopes(valuationContext.completeWithContextualProperties(propertyVisitors, true, true));
                String value = replaceMustachePropertiesWithValues(optValue.get(), scopes);
                // cf. BDD Scenario: get file with instance properties created by a module property that references itself and a global property with same name
                if (StringUtils.contains(value, "}}")) {
                    scopes = propertiesToScopes(valuationContext.completeWithContextualProperties(propertyVisitors, false, true));
                    value = replaceMustachePropertiesWithValues(value, scopes);
                    // cf. BDD Scenario: get file with property valorized with another valued property valorized with a predefined property
                    if (StringUtils.contains(value, "}}")) {
                        scopes = propertiesToScopes(valuationContext.completeWithContextualProperties(propertyVisitors));
                        value = replaceMustachePropertiesWithValues(value, scopes);
                    }
                }
                propertyVisitor = propertyVisitor.withValue(value);
            }
            return propertyVisitor;
        });
        return propertyVisitors.equals(preparedPropertyVisitors) ? preparedPropertyVisitors : preparePropertiesValues(preparedPropertyVisitors, valuationContext, iterationCount + 1);
    }

    // Juste avant d'appeler le moteur Mustache,
    // on supprime toutes les {{mustaches}} n'ayant pas déjà été substituées par `preparePropertiesValues` des valorisations,
    // afin que les fichiers générés n'en contiennent plus aucune trace
    private static PropertyVisitorsSequence removeMustachesInPropertyValues(PropertyVisitorsSequence propertyVisitors) {
        return propertyVisitors.mapSimplesRecursive(propertyVisitor -> {
            if (propertyVisitor.isValued()) {
                propertyVisitor = propertyVisitor.withValue(StringUtils.removeAll(propertyVisitor.getValue().get(), "\\{\\{[^}]*\\}\\}"));
            }
            return propertyVisitor;
        });
    }

    /**
     * Transforme une liste de propriétés `AbstractValuedPropertyView` pouvant contenir des propriétés simples
     * et des propriétés itérables en map de ce type :
     * - nom-propriété-simple => valeur-propriété-simple
     * - nom-propriété-itérable => list (...)
     */
    private static Map<String, Object> propertiesToScopes(PropertyVisitorsSequence preparedPropertyVisitors) {
        // On concatène les propriétés parentes avec les propriété de l'item
        // pour bénéficier de la valorisation de ces propriétés dans les propriétés filles
        // cf. BDD Scenario: get file with an iterable-ception
        PropertyVisitorsSequence completePropertyVisitors = preparedPropertyVisitors.mapSequencesRecursive(propertyVisitors -> {
            List<SimplePropertyVisitor> simpleSimplePropertyVisitors = propertyVisitors.getSimplePropertyVisitors();
            return propertyVisitors.mapDirectChildIterablePropertyVisitors(
                    iterablePropertyVisitor -> iterablePropertyVisitor.addPropertyVisitorsOrUpdateValue(simpleSimplePropertyVisitors)
            );
        });
        Map<String, Object> scopes = new HashMap<>();
        completePropertyVisitors.forEach(
                simplePropertyVisitor ->
                        simplePropertyVisitor.getMustacheKeyValues().forEach(scopes::put),
                iterablePropertyVisitor -> scopes.put(
                        iterablePropertyVisitor.getName(),
                        iterablePropertyVisitor.getItems().stream()
                                .map(FileUseCases::propertiesToScopes)
                                .collect(Collectors.toList()))
        );
        // cf. #540 & BDD Scenario: get file with instance properties created by a module property that references itself
        completePropertyVisitors.forEachSimplesRecursive(propertyVisitor -> {
            Optional<String> optValue = propertyVisitor.getValue();
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
