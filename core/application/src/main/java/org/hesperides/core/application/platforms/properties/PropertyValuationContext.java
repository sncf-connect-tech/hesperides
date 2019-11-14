package org.hesperides.core.application.platforms.properties;

import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitorsSequence;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.platforms.queries.views.InstanceView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.hesperides.core.application.platforms.properties.PropertyType.GLOBAL;
import static org.hesperides.core.application.platforms.properties.PropertyType.WITHOUT_MODEL;
import static org.hesperides.core.domain.platforms.entities.properties.ValuedPropertyTransformation.*;

@Value
public class PropertyValuationContext {

    private List<ValuedPropertyView> globalProperties;
    private List<ValuedPropertyView> instanceProperties;
    private List<ValuedPropertyView> predefinedProperties;
    private List<AbstractValuedPropertyView> valuedPropertiesWithoutModel;

    PropertyValuationContext(PlatformView platform, DeployedModuleView deployedModule, String instanceName, List<AbstractValuedPropertyView> valuedPropertiesWithoutModel) {
        globalProperties = platform.getGlobalProperties();
        instanceProperties = getInstanceProperties(deployedModule.getInstances(), deployedModule.getInstancesModel(), instanceName);
        predefinedProperties = getPredefinedProperties(platform, deployedModule, instanceName);
        this.valuedPropertiesWithoutModel = valuedPropertiesWithoutModel;
    }

    PropertyValuationContext(PlatformView platform, List<AbstractValuedPropertyView> valuedPropertiesWithoutModel) {
        globalProperties = platform.getGlobalProperties();
        instanceProperties = Collections.emptyList();
        predefinedProperties = getPredefinedProperties(platform, null, null);
        this.valuedPropertiesWithoutModel = valuedPropertiesWithoutModel;
    }

    // Pas la plus belle signature du monde...
    // Refacto en 3 méthodes distinctes ? -> nommage pas évident à choisir
    PropertyVisitorsSequence completeWithContextualProperties(PropertyVisitorsSequence propertyVisitors, EnumSet<PropertyType> propertiesToInclude) {
        // Concatène les propriétés globales, de module, d'instance et prédéfinies
        propertyVisitors = propertyVisitors.addOverridingValuedProperties(instanceProperties, OVERRIDDEN_BY_INSTANCE)
                .addOverridingValuedProperties(predefinedProperties, OVERRIDDEN_BY_PREDEFINED);
        if (propertiesToInclude.contains(GLOBAL)) {
            propertyVisitors = propertyVisitors.addOverridingValuedProperties(globalProperties, OVERRIDDEN_BY_GLOBAL);
        }
        if (propertiesToInclude.contains(WITHOUT_MODEL)) {
            propertyVisitors = propertyVisitors.addValuedPropertiesIfUndefined(valuedPropertiesWithoutModel.stream()
                    .filter(ValuedPropertyView.class::isInstance)
                    .map(ValuedPropertyView.class::cast));
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
        if (deployedModule != null) {
            predefinedProperties.add(new ValuedPropertyView("hesperides.module.name", deployedModule.getName()));
            predefinedProperties.add(new ValuedPropertyView("hesperides.module.version", deployedModule.getVersion()));
            String modulePath = deployedModule.getModulePath();
            predefinedProperties.add(new ValuedPropertyView("hesperides.module.path.full", modulePath.replace('#', '/')));
            predefinedProperties.addAll(getPathLogicalGroups(modulePath));
            predefinedProperties.add(new ValuedPropertyView("hesperides.instance.name", defaultString(instanceName, "")));
        }
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

    public PropertyVisitorsSequence removePredefinedProperties(PropertyVisitorsSequence propertyVisitors) {
        return propertyVisitors.removePropertiesByName(predefinedProperties.stream().map(ValuedPropertyView::getName).collect(Collectors.toSet()));
    }

    public PropertyVisitorsSequence removeGlobalPropertiesThatAreNotInTheModel(PropertyVisitorsSequence propertyVisitors, List<AbstractPropertyView> modulePropertiesModels) {
        return new PropertyVisitorsSequence(propertyVisitors.stream().filter(propertyVisitor -> {
            boolean isGlobal = globalProperties.stream().anyMatch(globalProperty -> globalProperty.getName().equals(propertyVisitor.getName()));
            boolean isNotInTheModel = modulePropertiesModels.stream().noneMatch(moduleProperty -> moduleProperty.getName().equals(propertyVisitor.getName()));
            return !(isGlobal && isNotInTheModel);
        }).collect(Collectors.toList()));
    }
}
