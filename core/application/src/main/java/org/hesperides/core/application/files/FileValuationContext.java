package org.hesperides.core.application.files;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.platforms.queries.views.InstanceView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Value
@Slf4j
public class FileValuationContext {

    private List<ValuedPropertyView> globalProperties;
    private List<ValuedPropertyView> instanceProperties;
    private List<ValuedPropertyView> predefinedProperties;
    private List<AbstractValuedPropertyView> extraValuedPropertiesWithoutModel;

    FileValuationContext(PlatformView platform, DeployedModuleView deployedModule, String instanceName, List<AbstractValuedPropertyView> extraValuedPropertiesWithoutModel) {
        globalProperties = platform.getGlobalProperties();
        instanceProperties = getInstanceProperties(deployedModule.getInstances(), deployedModule.getInstancesModel(), instanceName);
        predefinedProperties = getPredefinedProperties(platform, deployedModule, instanceName);
        this.extraValuedPropertiesWithoutModel = extraValuedPropertiesWithoutModel;
    }

    PropertyVisitorsSequence completeWithContextualProperties(PropertyVisitorsSequence propertyVisitors) {
        return completeWithContextualProperties(propertyVisitors, true);
    }

    PropertyVisitorsSequence completeWithContextualProperties(PropertyVisitorsSequence propertyVisitors, boolean withGlobals) {
        return completeWithContextualProperties(propertyVisitors, withGlobals, false);
    }

    // Pas la plus belle signature du monde...
    // Refacto en 3 méthodes distinctes ? -> nommage pas évident à choisir
    PropertyVisitorsSequence completeWithContextualProperties(PropertyVisitorsSequence propertyVisitors, boolean withGlobals, boolean withExtraPropsWithoutModel) {
        // Concatène les propriétés globales, de module, d'instance et prédéfinies
        propertyVisitors = propertyVisitors.addOverridingValuedProperties(instanceProperties)
                .addOverridingValuedProperties(predefinedProperties);
        if (withGlobals) {
            propertyVisitors = propertyVisitors.addOverridingValuedProperties(globalProperties);
        }
        if (withExtraPropsWithoutModel) {
            propertyVisitors = propertyVisitors.addValuedPropertiesIfUndefined(extraValuedPropertiesWithoutModel.stream()
                    .filter(ValuedPropertyView.class::isInstance).map(ValuedPropertyView.class::cast));
        }
        return propertyVisitors;
    }

    private static List<ValuedPropertyView> getInstanceProperties(List<InstanceView> instances, List<String> instancesModel, String instanceName) {
        return instances.stream()
                .filter(instance -> instance.getName().equalsIgnoreCase(instanceName))
                .findFirst()
                .map(InstanceView::getValuedProperties)
                .orElseGet(Collections::emptyList)
                .stream()
                // #539 La propriété d'instance doit faire partie du model d'instances
                .filter(instanceProperty -> instancesModel.contains(instanceProperty.getName()))
                .collect(Collectors.toList());
    }

    private static List<ValuedPropertyView> getPredefinedProperties(PlatformView platform, DeployedModuleView deployedModule, String instanceName) {
        List<ValuedPropertyView> predefinedProperties = new ArrayList<>();
        predefinedProperties.add(new ValuedPropertyView("hesperides.application.name", platform.getApplicationName()));
        predefinedProperties.add(new ValuedPropertyView("hesperides.application.version", platform.getVersion()));
        predefinedProperties.add(new ValuedPropertyView("hesperides.platform.name", platform.getPlatformName()));
        predefinedProperties.add(new ValuedPropertyView("hesperides.module.name", deployedModule.getName()));
        predefinedProperties.add(new ValuedPropertyView("hesperides.module.version", deployedModule.getVersion()));
        String modulePath = deployedModule.getModulePath();
        predefinedProperties.add(new ValuedPropertyView("hesperides.module.path.full", modulePath.replace('#', '/')));
        predefinedProperties.addAll(getPathLogicalGroups(modulePath));
        predefinedProperties.add(new ValuedPropertyView("hesperides.instance.name", instanceName));
        return predefinedProperties;
    }

    private static List<ValuedPropertyView> getPathLogicalGroups(String modulePath) {
        List<ValuedPropertyView> pathLogicalGroups = new ArrayList<>();
        String[] groups = modulePath.split("#");
        for (int index = 1; index < groups.length; index++) {
            pathLogicalGroups.add(new ValuedPropertyView("hesperides.module.path." + (index - 1), groups[index]));
        }
        return pathLogicalGroups;
    }
}
