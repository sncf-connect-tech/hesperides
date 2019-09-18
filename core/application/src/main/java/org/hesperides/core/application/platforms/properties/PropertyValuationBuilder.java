package org.hesperides.core.application.platforms.properties;

import com.github.mustachejava.Mustache;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.application.files.FileUseCases;
import org.hesperides.core.application.files.InfiniteMustacheRecursion;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.entities.properties.PropertyType;
import org.hesperides.core.domain.platforms.entities.properties.ValuedPropertyTransformation;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitor;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitorsSequence;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.entities.AbstractProperty;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.hesperides.core.domain.platforms.entities.properties.PropertyType.*;
import static org.hesperides.core.domain.platforms.entities.properties.ValuedPropertyTransformation.*;
import static org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView.hidePasswordProperties;

public class PropertyValuationBuilder {

    private static int MAX_PREPARE_PROPERTIES_COUNT = 10;

    public static PropertyVisitorsSequence buildPropertyVisitorsSequence(PlatformView platform,
                                                                         String modulePath,
                                                                         Module.Key moduleKey,
                                                                         List<AbstractPropertyView> modulePropertiesModels,
                                                                         String instanceName,
                                                                         boolean shouldHidePasswordProperties,
                                                                         EnumSet<PropertyType> propertiesToInclude) {

        DeployedModuleView deployedModule = platform.getDeployedModule(modulePath, moduleKey);

        List<AbstractValuedPropertyView> valuedProperties = deployedModule.getValuedProperties();
        if (shouldHidePasswordProperties) {
            valuedProperties = hidePasswordProperties(valuedProperties, modulePropertiesModels);
        }
        PropertyVisitorsSequence propertyVisitors = PropertyVisitorsSequence.fromModelAndValuedProperties(modulePropertiesModels, valuedProperties, propertiesToInclude.contains(WITHOUT_MODEL));
        List<AbstractValuedPropertyView> valuedPropertiesWithoutModel = extractValuedPropertiesWithoutModel(valuedProperties, propertyVisitors);
        // A ce stade, `valuedProperties` ne contient plus que les valorisations de propriétés sans modèle associé
        PropertyValuationContext valuationContext = new PropertyValuationContext(platform, deployedModule, instanceName, valuedPropertiesWithoutModel);
        PropertyVisitorsSequence completedPropertyVisitors = valuationContext.completeWithContextualProperties(propertyVisitors, propertiesToInclude.contains(GLOBAL), propertiesToInclude.contains(WITHOUT_MODEL));
        // Prépare les propriétés faisant référence à d'autres propriétés, de manière récursive :
        propertyVisitors = preparePropertiesValues(completedPropertyVisitors, valuationContext, 0);
        if (!propertiesToInclude.contains(PREDEFINED)) {
            propertyVisitors = valuationContext.removePredefinedProperties(propertyVisitors);
        }
        return propertyVisitors;
    }

    public static PropertyVisitorsSequence buildPropertyVisitorsSequenceForGlobals(PlatformView platform) {
        PropertyVisitorsSequence propertyVisitors = PropertyVisitorsSequence.fromModelAndValuedProperties(Collections.emptyList(), platform.getGlobalProperties(), true);
        List<AbstractValuedPropertyView> valuedPropertiesWithoutModel = extractValuedPropertiesWithoutModel(platform.getGlobalProperties(), propertyVisitors);
        PropertyValuationContext valuationContext = new PropertyValuationContext(platform, valuedPropertiesWithoutModel);
        PropertyVisitorsSequence completedPropertyVisitors = valuationContext.completeWithContextualProperties(propertyVisitors, true, true);
        propertyVisitors = preparePropertiesValues(completedPropertyVisitors, valuationContext, 0);
        propertyVisitors = valuationContext.removePredefinedProperties(propertyVisitors);
        return propertyVisitors;
    }

    private static List<AbstractValuedPropertyView> extractValuedPropertiesWithoutModel(List<? extends AbstractValuedPropertyView> valuedProperties,
                                                                                        PropertyVisitorsSequence propertyVisitors) {
        Set<String> propertyWithModelNames = propertyVisitors.getProperties().stream()
                .map(PropertyVisitor::getName)
                .collect(Collectors.toSet());
        return valuedProperties.stream()
                .filter(valuedProperty -> !propertyWithModelNames.contains(valuedProperty.getName()))
                .collect(Collectors.toList());
    }

    private static PropertyVisitorsSequence preparePropertiesValues(PropertyVisitorsSequence propertyVisitors,
                                                                    PropertyValuationContext valuationContext,
                                                                    int iterationCount) {
        if (iterationCount > MAX_PREPARE_PROPERTIES_COUNT) {
            throw new InfiniteMustacheRecursion("Infinite loop due to self-referencing property or template");
        }
        PropertyVisitorsSequence preparedPropertyVisitors = propertyVisitors.mapSimplesRecursive(propertyVisitor -> {
            Optional<String> optValue = propertyVisitor.getValueOrDefault();
            if (optValue.isPresent() && StringUtils.contains(optValue.get(), "}}")) { // not bullet-proof but a false positive on mustaches escaped by a delimiter set is OK
                // iso-legacy: on inclue les valorisations sans modèle ici
                // cf. BDD Scenario: get file with property valorized with another valued property
                Map<String, Object> scopes = FileUseCases
                        .propertiesToScopes(valuationContext.completeWithContextualProperties(propertyVisitors, true, true)
                                .passOverPropertyValuesToChildItems());
                String value = replaceMustachePropertiesWithValues(optValue.get(), scopes);
                // Principe de substitution : une propriété qui en réference une autre doit prendre sa valeur,
                // en sachant que cette valeur peut elle-même e, référencer une autre...
                // Le "niveau" indique le nombre de remplacements de ce type.
                ValuedPropertyTransformation transformation = PROPERTY_SUBSTITUTION_LEVEL_1;
                // cf. BDD Scenario: get file with instance properties created by a module property that references itself and a global property with same name
                if (StringUtils.contains(value, "}}")) {
                    scopes = FileUseCases
                            .propertiesToScopes(valuationContext.completeWithContextualProperties(propertyVisitors, false, true)
                                    .passOverPropertyValuesToChildItems());
                    value = replaceMustachePropertiesWithValues(value, scopes);
                    transformation = PROPERTY_SUBSTITUTION_LEVEL_2;
                    // cf. BDD Scenario: get file with property valorized with another valued property valorized with a predefined property
                    if (StringUtils.contains(value, "}}")) {
                        scopes = FileUseCases
                                .propertiesToScopes(valuationContext.completeWithContextualProperties(propertyVisitors, true, false)
                                        .passOverPropertyValuesToChildItems());
                        value = replaceMustachePropertiesWithValues(value, scopes);
                        transformation = PROPERTY_SUBSTITUTION_LEVEL_3;
                    }
                }
                propertyVisitor = propertyVisitor.withValue(value, transformation);
            }
            return propertyVisitor;
        });
        return propertyVisitors.equals(preparedPropertyVisitors)
                ? preparedPropertyVisitors
                : preparePropertiesValues(preparedPropertyVisitors, valuationContext, iterationCount + 1);
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
