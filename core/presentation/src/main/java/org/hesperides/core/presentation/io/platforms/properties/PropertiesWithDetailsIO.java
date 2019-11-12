package org.hesperides.core.presentation.io.platforms.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.domain.platforms.entities.properties.PropertyWithDetails;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class PropertiesWithDetailsIO {

    // Annotation @NotNull à remettre en place lorsque le support d'un payload json sans properties_version_id sera officiellement arrêté
    @SerializedName("properties_version_id")
    @JsonProperty("properties_version_id")
    @Valid
    Long propertiesVersionId;

    @NotNull
    @SerializedName("key_value_properties")
    @JsonProperty("key_value_properties")
    @Valid
    Set<PropertyWithDetailsIO> valuedProperties;

    @NotNull
    @SerializedName("iterable_properties")
    @JsonProperty("iterable_properties")
    @Valid
    Set<IterableValuedPropertyIO> iterableValuedProperties;

    public PropertiesWithDetailsIO(@Valid Long propertiesVersionId, @Valid Set<PropertyWithDetailsIO> valuedProperties, @Valid List<AbstractValuedPropertyView> abstractValuedPropertyViews) {
        this.propertiesVersionId = propertiesVersionId;
        List<IterableValuedPropertyView> iterableValuedPropertyViews = AbstractValuedPropertyView.getAbstractValuedPropertyViewWithType(abstractValuedPropertyViews, IterableValuedPropertyView.class);
        this.iterableValuedProperties = IterableValuedPropertyIO.fromIterableValuedPropertyViews(iterableValuedPropertyViews);
        this.valuedProperties = valuedProperties;
    }

    public Long getPropertiesVersionId() {
        return propertiesVersionId != null ? propertiesVersionId : DeployedModule.INIT_PROPERTIES_VERSION_ID;
    }

    public static Set<PropertyWithDetailsIO> toPropertiesWithDetailIsO(List<PropertyWithDetails> propertiesWithDetails) {
        return propertiesWithDetails.stream()
                .map(propertyWithDetails -> new PropertyWithDetailsIO(propertyWithDetails.getName(), propertyWithDetails.getStoredValue(),
                        propertyWithDetails.getFinalValue(), propertyWithDetails.getDefaultValue(), propertyWithDetails.getTransformations()))
                .collect(Collectors.toSet());
    }
}
