package org.hesperides.core.domain.platforms.queries.views.properties;

import lombok.Value;
import org.hesperides.core.domain.modules.queries.ModuleSimplePropertiesView;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;

import java.util.*;
import java.util.stream.Collectors;

@Value
public class GlobalPropertyUsageView {

    boolean inModel;
    String propertiesPath;

    /**
     * Récupère la liste des propriétés globales utilisées dans les modules
     */
    public static List<GlobalPropertyUsageView> getModuleGlobalProperties(final List<AbstractPropertyView> moduleProperties,
                                                                          final String globalPropertyName,
                                                                          final String propertiesPath) {
        return moduleProperties.stream()
                // TODO Sensible à la casse ?
                .filter(moduleProperty -> moduleProperty.getName().equals(globalPropertyName))
                .map(moduleProperty -> new GlobalPropertyUsageView(true, propertiesPath))
                .collect(Collectors.toList());
    }

    /**
     * Retourne la liste des utilisations d'une propriété globale.
     * Il existe 2 façons d'utiliser une propriété globale :
     * - En tant que propriété dans un template
     * - En tant que valeur de propriété d'un module déployé
     *
     * Le critère inModel est déterminé comme ceci :
     *
     * Si la propriété globale est utilisée en tant que valeur
     * d'une propriété au niveau du module, et que cette propriété
     * initialement déclarée dans le template ne fait plus partie
     * de la liste des propriétés simples du model, inModel = false.
     *
     * Dans tous les autres cas, inModel = true;
     */
    public static Set<GlobalPropertyUsageView> getGlobalPropertyUsage(String globalPropertyName, List<DeployedModuleView> deployedModules, List<ModuleSimplePropertiesView> modulesSimpleProperties) {
        Set<GlobalPropertyUsageView> globalPropertyUsages = new HashSet<>();

        deployedModules.forEach(deployedModule -> {
            List<PropertyView> moduleSimpleProperties = getModuleSimpleProperties(modulesSimpleProperties, deployedModule.getModuleKey());
            String propertiesPath = deployedModule.getPropertiesPath();

            if (propertyNameIsInProperties(globalPropertyName, moduleSimpleProperties)) {
                globalPropertyUsages.add(new GlobalPropertyUsageView(true, propertiesPath));

            } else {
                Optional<String> valuedProperty = getValuedPropertyUsingGlobalPropertyAsValue(globalPropertyName, deployedModule.getValuedProperties());
                if (valuedProperty.isPresent()) {
                    boolean inModel = propertyNameIsInProperties(valuedProperty.get(), moduleSimpleProperties);
                    globalPropertyUsages.add(new GlobalPropertyUsageView(inModel, propertiesPath));
                }
            }
        });
        return globalPropertyUsages;
    }

    private static Optional<String> getValuedPropertyUsingGlobalPropertyAsValue(String globalPropertyName, List<AbstractValuedPropertyView> valuedProperties) {
        return valuedProperties
                .stream()
                .filter(ValuedPropertyView.class::isInstance)
                .map(ValuedPropertyView.class::cast)
                .filter(simpleProperty -> simpleProperty.getValue().toLowerCase().contains("{{" + globalPropertyName.toLowerCase() + "}}"))
                .findFirst()
                .map(ValuedPropertyView::getName);
    }

    private static List<PropertyView> getModuleSimpleProperties(List<ModuleSimplePropertiesView> modulesSimpleProperties, TemplateContainer.Key moduleKey) {
        return modulesSimpleProperties
                .stream()
                .filter(moduleModel -> moduleModel.getModuleKey().equals(moduleKey))
                .findFirst()
                .map(ModuleSimplePropertiesView::getProperties)
                .orElse(Collections.emptyList());
    }

    private static boolean propertyNameIsInProperties(String propertyName, List<PropertyView> moduleProperties) {
        return moduleProperties.stream()
                .anyMatch(propertyView -> propertyView.getName().equalsIgnoreCase(propertyName));
    }
}
