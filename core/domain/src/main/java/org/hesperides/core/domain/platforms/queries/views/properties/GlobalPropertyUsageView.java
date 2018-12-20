package org.hesperides.core.domain.platforms.queries.views.properties;

import lombok.Value;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;

import java.util.List;
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
     * Récupère la liste des propriétés globales utilisées en tant que valeur
     * lors de la valorisation des propriétés des modules déployés
     */
    public static List<GlobalPropertyUsageView> getDeployedModuleGlobalProperties(final DeployedModuleView deployedModule,
                                                                                  final String globalPropertyName,
                                                                                  final List<AbstractPropertyView> moduleProperties,
                                                                                  boolean moduleExists) {
        return AbstractValuedPropertyView.flattenValuedProperties(deployedModule.getValuedProperties())
                .stream()
                .map(ValuedPropertyView.class::cast)
                .filter(deployedModuleProperty -> deployedModuleProperty.getValue().contains("{{" + globalPropertyName + "}}"))
                .map(deployedModuleProperty -> new GlobalPropertyUsageView(
                        valuedPropertyIsInModel(moduleProperties, deployedModuleProperty.getName(), moduleExists),
                        deployedModule.getPropertiesPath()))
                .collect(Collectors.toList());
    }

    /**
     * Vrai si la propriété se trouve à la fois dans le module déployé et dans le model du module
     */
    private static boolean valuedPropertyIsInModel(final List<AbstractPropertyView> moduleProperties,
                                                   final String valuedPropertyName,
                                                   final boolean moduleExists) {
        boolean isInModel = false;
        if (moduleExists) {
            isInModel = moduleProperties.stream()
                    .anyMatch(moduleProperty -> valuedPropertyName.equals(moduleProperty.getName()));
        }
        return isInModel;
    }
}
