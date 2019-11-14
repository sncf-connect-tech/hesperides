package org.hesperides.core.presentation.io.platforms.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.platforms.queries.views.properties.PropertyWithDetailsView;

import java.util.List;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class PropertiesWithDetailsOutput {

    @SerializedName("key_value_properties")
    @JsonProperty("key_value_properties")
    List<PropertyWithDetailsOutput> valuedProperties;

    public static PropertiesWithDetailsOutput fromViews(List<PropertyWithDetailsView> propertiesWithDetails) {
        List<PropertyWithDetailsOutput> valuedProperties = propertiesWithDetails
                .stream()
                .map(PropertyWithDetailsOutput::new)
                .collect(Collectors.toList());
        return new PropertiesWithDetailsOutput(valuedProperties);
    }
}
