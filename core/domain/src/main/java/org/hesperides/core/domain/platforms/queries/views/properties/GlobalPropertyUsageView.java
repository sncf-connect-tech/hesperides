package org.hesperides.core.domain.platforms.queries.views.properties;

import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Value
public class GlobalPropertyUsageView {

    boolean isRemovedFromTemplate;
    String propertiesPath;

    /**
     * Retourne la liste des utilisations d'une propriété globale.
     * Il existe 2 façons d'utiliser une propriété globale :
     * - En tant que propriété dans un template
     * - En tant que valeur de propriété d'un module déployé
     * <p>
     * Le critère isRemovedFromTemplate est déterminé comme ceci :
     * <p>
     * Si la propriété globale existe dans les propriétés du module
     * ou est utilisée en tant que valeur d'une propriété
     * au niveau du module, et que cette propriété initialement déclarée dans le
     * template ne fait plus partie de la liste des propriétés simples (par opposition
     * aux propriétés itérables) du model, isRemovedFromTemplate = true.
     * <p>
     * Dans tous les autres cas, isRemovedFromTemplate = false;
     */
    public static Set<GlobalPropertyUsageView> getGlobalPropertyUsage(String globalPropertyName, List<DeployedModuleView> deployedModules, List<AbstractPropertyView> abstractPropertyViews) {
        Set<GlobalPropertyUsageView> globalPropertyUsages = new HashSet<>();

        deployedModules.forEach(deployedModule -> {
            List<PropertyView> modulesProperties = getModulesPropertiesViews(abstractPropertyViews, deployedModule.getModuleKey());
            String propertiesPath = deployedModule.getPropertiesPath();

            if (propertyNameIsInProperties(globalPropertyName, modulesProperties)) {
                globalPropertyUsages.add(new GlobalPropertyUsageView(false, propertiesPath));
            }

            List<String> valuedPropertiesName = getIterablesValudPropertiesNamesUsingGlobalProperty(globalPropertyName, deployedModule.getValuedProperties());
            if (!CollectionUtils.isEmpty(valuedPropertiesName)) {
                valuedPropertiesName.forEach(valuedPropertyName -> {
                    boolean isRemovedFromTemplate = !propertyNameIsInProperties(valuedPropertyName, modulesProperties);
                    globalPropertyUsages.add(new GlobalPropertyUsageView(isRemovedFromTemplate, propertiesPath));
                });
            }
        });
        return globalPropertyUsages;
    }

    private static List<PropertyView> getModulesPropertiesViews(List<AbstractPropertyView> abstractPropertyViews, TemplateContainer.Key moduleKey) {
        Stream<PropertyView> propertyViewStream = AbstractPropertyView.getFlatProperties(abstractPropertyViews);
        List<PropertyView> propertyViews = propertyViewStream.collect(Collectors.toList());
        return propertyViews;
    }


    private static List<String> getIterablesValudPropertiesNamesUsingGlobalProperty(String globalPropertyName, List<AbstractValuedPropertyView> valuedProperties) {
        return valuedProperties.stream()
                .filter(abstractValuedPropertyView -> globalPropertyIsUsedInIterablesProperties(globalPropertyName, abstractValuedPropertyView))
                .map(abstractValuedPropertyView -> abstractValuedPropertyView.getName()).collect(Collectors.toList());
    }


    private static boolean globalPropertyIsUsedInValuedProperty(ValuedPropertyView valuedProperty, String globalPropertyName) {
        List<String> valuesBetweenCurlyBrackets = ValuedProperty.extractValuesBetweenCurlyBrackets(valuedProperty.getValue());
        return valuedProperty.getName().equals(globalPropertyName) || valuesBetweenCurlyBrackets.contains(globalPropertyName);
    }

    private static boolean globalPropertyIsUsedInIterablesProperties(String globalPropertyName, AbstractValuedPropertyView abstractValuedPropertyView) {
        if (abstractValuedPropertyView instanceof ValuedPropertyView) {
            return globalPropertyIsUsedInValuedProperty((ValuedPropertyView) abstractValuedPropertyView, globalPropertyName);
        } else {
            IterableValuedPropertyView iterableValuedPropertyView = (IterableValuedPropertyView) abstractValuedPropertyView;
            return iterableValuedPropertyView.getIterablePropertyItems()
                    .stream().anyMatch(iterablePropertyItemView -> iterablePropertyItemView.getAbstractValuedPropertyViews()
                            .stream().anyMatch(abstractValuedPropertyView1 -> globalPropertyIsUsedInIterablesProperties(globalPropertyName, abstractValuedPropertyView1)));
        }
    }

    private static boolean propertyNameIsInProperties(String propertyName, List<PropertyView> moduleProperties) {
        return moduleProperties.stream()
                .anyMatch(propertyView -> propertyView.getName().equals(propertyName));
    }
}
