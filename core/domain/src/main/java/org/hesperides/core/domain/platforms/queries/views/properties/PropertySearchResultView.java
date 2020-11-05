package org.hesperides.core.domain.platforms.queries.views.properties;

import lombok.Value;
import org.hesperides.core.domain.modules.entities.Module;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

@Value
public class PropertySearchResultView {
    String propertyName;
    String propertyValue;
    String applicationName;
    String platformName;
    boolean isProductionPlatform;
    String propertiesPath;

    public PropertySearchResultView hideProductionPasswordOrExcludeIfSearchedByValue(
            Map<Module.Key, List<String>> passwordsByModule,
            boolean isSearchByValue) {

        Module.Key moduleKey = Module.Key.fromPropertiesPath(propertiesPath);
        List<String> modulePasswords = passwordsByModule.getOrDefault(moduleKey, emptyList());
        boolean isProductionPassword = isProductionPlatform && modulePasswords.contains(propertyName);

        String filteredPropertyValue = isProductionPassword ? "******" : propertyValue;
        boolean excludeProperty = isProductionPassword && isSearchByValue;

        return excludeProperty ? null : new PropertySearchResultView(
                propertyName,
                filteredPropertyValue,
                applicationName,
                platformName,
                isProductionPlatform,
                propertiesPath);
    }
}
