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
import java.util.Set;
import java.util.stream.Collectors;

@Value
@Slf4j
public class FileValuationContext {

    private List<ValuedPropertyView> globalProperties;
    private List<ValuedPropertyView> instanceProperties;
    private List<ValuedPropertyView> predefinedProperties;
    private String deployedModuleDescriptor;

    FileValuationContext(PlatformView platform, DeployedModuleView deployedModule, String instanceName) {
        globalProperties = platform.getGlobalProperties();
        instanceProperties = getInstanceProperties(deployedModule.getInstances(), deployedModule.getInstancesModel(), instanceName);
        predefinedProperties = getPredefinedProperties(platform, deployedModule, instanceName);
        deployedModuleDescriptor = platform.getApplicationName() + "-" + platform.getPlatformName() + " " + deployedModule.getPropertiesPath();
    }

    List<AbstractValuedPropertyView> completeWithContextualProperties(List<AbstractValuedPropertyView> properties, boolean withGlobals) {
        // Concatène les propriétés globales, de module, d'instance et prédéfinies
        properties = concat(properties, getPredefinedProperties(), "a predefined property");
        properties = concat(properties, getInstanceProperties(), "an instance property");
        if (withGlobals) {
            properties = concat(properties, getGlobalProperties(), "a global property");
        }
        return properties;
    }

    private List<AbstractValuedPropertyView> concat(List<? extends AbstractValuedPropertyView> listOfProps1,
                                                    List<? extends AbstractValuedPropertyView> listOfProps2,
                                                    String overridingPropIdForWarning) {
        List<AbstractValuedPropertyView> properties = new ArrayList<>(listOfProps2);
        Set<String> replacableStrings = properties.stream()
                .map(AbstractValuedPropertyView::getName)
                .collect(Collectors.toSet());
        for (AbstractValuedPropertyView property : listOfProps1) {
            // !! Ne pas inverser cette condition !!
            // Si le log.debug reste seul dans le `if`,
            // nous avons observé que la clause `else` est exécutée à la place
            if (!replacableStrings.contains(property.getName())) {
                properties.add(property);
            } else {
                log.debug("{}: During valorization, {} was overriden by {} with same name",
                        deployedModuleDescriptor, property.getName(), overridingPropIdForWarning);
            }
        }
        return properties;
    }

    private static List<ValuedPropertyView> getInstanceProperties(List<InstanceView> instances, List<String> instancesModel, String instanceName) {
        return instances.stream()
                .filter(instance -> instance.getName().equalsIgnoreCase(instanceName))
                .findFirst()
                .map(InstanceView::getValuedProperties)
                .orElse(Collections.emptyList())
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
