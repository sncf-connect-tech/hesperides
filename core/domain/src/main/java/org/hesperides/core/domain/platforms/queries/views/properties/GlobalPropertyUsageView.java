package org.hesperides.core.domain.platforms.queries.views.properties;

import lombok.Value;
import org.hesperides.core.domain.modules.queries.ModuleSimplePropertiesView;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public static Set<GlobalPropertyUsageView> getGlobalPropertyUsage(String globalPropertyName, List<DeployedModuleView> deployedModules, List<ModuleSimplePropertiesView> modulesSimpleProperties) {
        Set<GlobalPropertyUsageView> globalPropertyUsages = new HashSet<>();

        deployedModules.forEach(deployedModule -> {
            List<PropertyView> moduleSimpleProperties = getModuleSimpleProperties(modulesSimpleProperties, deployedModule.getModuleKey());
            String propertiesPath = deployedModule.getPropertiesPath();

            if (propertyNameIsInProperties(globalPropertyName, moduleSimpleProperties)) {
                globalPropertyUsages.add(new GlobalPropertyUsageView(false, propertiesPath));

            }
            List<String> valuedPropertiesName = getSimpleValuedPropertiesNameUsingGlobalProperty(globalPropertyName, deployedModule.getValuedProperties());
            if (!CollectionUtils.isEmpty(valuedPropertiesName)) {
                valuedPropertiesName.forEach(valuedPropertyName -> {
                    boolean isRemovedFromTemplate = !propertyNameIsInProperties(valuedPropertyName, moduleSimpleProperties);
                    globalPropertyUsages.add(new GlobalPropertyUsageView(isRemovedFromTemplate, propertiesPath));
                });
            }
        });
        return globalPropertyUsages;
    }

    private static List<PropertyView> getModuleSimpleProperties(List<ModuleSimplePropertiesView> modulesSimpleProperties, TemplateContainer.Key moduleKey) {
        return modulesSimpleProperties
                .stream()
                .filter(moduleModel -> moduleModel.getModuleKey().equals(moduleKey))
                .findFirst()
                .map(ModuleSimplePropertiesView::getProperties)
                .orElse(Collections.emptyList());
    }

    private static List<String> getSimpleValuedPropertiesNameUsingGlobalProperty(String globalPropertyName, List<AbstractValuedPropertyView> valuedProperties) {
        return valuedProperties.stream()
                .filter(ValuedPropertyView.class::isInstance)
                .map(ValuedPropertyView.class::cast)
                .filter(valuedProperty -> globalPropertyIsUsedInValuedProperty(valuedProperty, globalPropertyName))
                .map(ValuedPropertyView::getName)
                .collect(Collectors.toList());
    }

    private static boolean globalPropertyIsUsedInValuedProperty(ValuedPropertyView valuedProperty, String globalPropertyName) {
        List<String> valuesBetweenCurlyBrackets = ValuedProperty.extractValuesBetweenCurlyBrackets(valuedProperty.getValue());
        return valuedProperty.getName().equals(globalPropertyName) || valuesBetweenCurlyBrackets.contains(globalPropertyName);
    }

    private static boolean propertyNameIsInProperties(String propertyName, List<PropertyView> moduleProperties) {
        return moduleProperties.stream()
                .anyMatch(propertyView -> propertyView.getName().equals(propertyName));
    }
}
