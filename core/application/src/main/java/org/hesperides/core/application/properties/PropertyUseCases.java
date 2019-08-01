package org.hesperides.core.application.properties;

import com.github.mustachejava.Mustache;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.application.files.*;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitor;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitorsSequence;
import org.hesperides.core.domain.platforms.entities.properties.visitors.SimplePropertyVisitor;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.entities.AbstractProperty;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView.hidePasswordProperties;

public class PropertyUseCases {

    private static int MAX_PREPARE_PROPERTIES_COUNT = 10;

    public static PropertyVisitorsSequence buildPropertyVisitorsSequence(PlatformView platform,
                                                                         String modulePath,
                                                                         Module.Key moduleKey,
                                                                         List<AbstractPropertyView> modulePropertiesModels,
                                                                         String instanceName,
                                                                         boolean shouldHidePasswordProperties) {
        return buildPropertyVisitorsSequence(platform, modulePath, moduleKey,
                modulePropertiesModels, instanceName, shouldHidePasswordProperties, false);
    }

    public static PropertyVisitorsSequence buildPropertyVisitorsSequence(PlatformView platform,
                                                                         String modulePath,
                                                                         Module.Key moduleKey,
                                                                         List<AbstractPropertyView> modulePropertiesModels,
                                                                         String instanceName,
                                                                         boolean shouldHidePasswordProperties,
                                                                         boolean excludePreparedProperties) {

        DeployedModuleView deployedModule = platform.getDeployedModule(modulePath, moduleKey);

        List<AbstractValuedPropertyView> valuedProperties = deployedModule.getValuedProperties();
        if (shouldHidePasswordProperties) {
            valuedProperties = hidePasswordProperties(valuedProperties, modulePropertiesModels);
        }
        PropertyVisitorsSequence propertyVisitors = PropertyVisitorsSequence.fromModelAndValuedProperties(modulePropertiesModels, valuedProperties);
        List<AbstractValuedPropertyView> extraValuedPropertiesWithoutModel = extractValuedPropertiesWithoutModel(valuedProperties, propertyVisitors);
        // A ce stade, `valuedProperties` ne contient plus que les valorisations de propriétés sans modèle associé
        PropertyValuationContext valuationContext = new PropertyValuationContext(platform, deployedModule, instanceName, extraValuedPropertiesWithoutModel);
        PropertyVisitorsSequence completedPropertyVisitors = valuationContext.completeWithContextualProperties(propertyVisitors);
        // Prépare les propriétés faisant référence à d'autres propriétés, de manière récursive :
        propertyVisitors = preparePropertiesValues(completedPropertyVisitors, valuationContext, 0);
        if (excludePreparedProperties) {
            propertyVisitors = valuationContext.removePreparedProperties(propertyVisitors);
        }
        return propertyVisitors;
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
                                                                    PropertyValuationContext valuationContext,
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

    /**
     * Transforme une liste de propriétés `AbstractValuedPropertyView` pouvant contenir des propriétés simples
     * et des propriétés itérables en map de ce type :
     * - nom-propriété-simple => valeur-propriété-simple
     * - nom-propriété-itérable => list (...)
     */
    public static Map<String, Object> propertiesToScopes(PropertyVisitorsSequence preparedPropertyVisitors) {
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
                                .map(PropertyUseCases::propertiesToScopes)
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
    public static String replaceMustachePropertiesWithValues(String input, Map<String, Object> scopes) {
        Mustache mustache = AbstractProperty.getMustacheInstanceFromStringContent(input);
        StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, scopes);
        stringWriter.flush();
        return stringWriter.toString();
    }
}
