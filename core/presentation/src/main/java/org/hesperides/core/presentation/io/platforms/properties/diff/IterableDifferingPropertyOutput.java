package org.hesperides.core.presentation.io.platforms.properties.diff;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.diff.IterableDifferingProperty;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
public class IterableDifferingPropertyOutput extends AbstractDifferingPropertyOutput {

    @SerializedName("differing_items")
    @JsonProperty("differing_items")
    List<PropertiesDiffOutput> differingItems;

    public IterableDifferingPropertyOutput(IterableDifferingProperty iterableDifferingProperty) {
        super(iterableDifferingProperty.getName());
        this.differingItems = PropertiesDiffOutput.fromPropertiesDiffs(iterableDifferingProperty.getDifferingItems());
    }
}
