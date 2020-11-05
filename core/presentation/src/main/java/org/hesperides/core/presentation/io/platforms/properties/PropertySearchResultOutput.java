package org.hesperides.core.presentation.io.platforms.properties;

import lombok.Value;
import org.hesperides.core.domain.platforms.queries.views.properties.PropertySearchResultView;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Value
public class PropertySearchResultOutput {

    String propertyName;
    String propertyValue;
    String applicationName;
    String platformName;
    String propertiesPath;

    public PropertySearchResultOutput(PropertySearchResultView view) {
        propertyName = view.getPropertyName();
        propertyValue = view.getPropertyValue();
        applicationName = view.getApplicationName();
        platformName = view.getPlatformName();
        propertiesPath = view.getPropertiesPath();
    }

    public static List<PropertySearchResultOutput> fromViews(List<PropertySearchResultView> properties) {
        return properties.stream().map(PropertySearchResultOutput::new).collect(toList());
    }
}
