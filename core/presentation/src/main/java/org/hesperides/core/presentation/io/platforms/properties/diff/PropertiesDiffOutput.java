package org.hesperides.core.presentation.io.platforms.properties.diff;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff;
import org.hesperides.core.presentation.io.platforms.properties.AbstractValuedPropertyIO;

import javax.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@NonFinal
public class PropertiesDiffOutput {
    @SerializedName("only_left")
    @JsonProperty("only_left")
    @NotEmpty
    Set<AbstractValuedPropertyIO> onlyLeft;

    @SerializedName("only_right")
    @JsonProperty("only_right")
    @NotEmpty
    Set<AbstractValuedPropertyIO> onlyRight;

    @SerializedName("common")
    @JsonProperty("common")
    @NotEmpty
    Set<AbstractValuedPropertyIO> common;

    @SerializedName("differing")
    @JsonProperty("differing")
    @NotEmpty
    Set<AbstractDifferingPropertyOutput> differing;

    public PropertiesDiffOutput(PropertiesDiff propertiesDiff) {
        this.onlyLeft = AbstractValuedPropertyIO.fromAbstractValuedProperties(propertiesDiff.getOnlyLeft());
        this.onlyRight = AbstractValuedPropertyIO.fromAbstractValuedProperties(propertiesDiff.getOnlyRight());
        this.common = AbstractValuedPropertyIO.fromAbstractValuedProperties(propertiesDiff.getCommon());
        this.differing = AbstractDifferingPropertyOutput.fromAbstractDifferingProperties(propertiesDiff.getDifferingProperties());
    }

    public static List<PropertiesDiffOutput> fromPropertiesDiffs(List<PropertiesDiff> propertiesDiffs) {
        return Optional.ofNullable(propertiesDiffs)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(PropertiesDiffOutput::new)
                .collect(Collectors.toList());
    }
}
