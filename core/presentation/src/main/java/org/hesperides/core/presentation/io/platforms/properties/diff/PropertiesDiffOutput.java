package org.hesperides.core.presentation.io.platforms.properties.diff;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitorsSequence;

import javax.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@NonFinal
@AllArgsConstructor
public class PropertiesDiffOutput {
    @SerializedName("only_left")
    @JsonProperty("only_left")
    @NotEmpty
    Set<AbstractDifferingPropertyOutput> onlyLeft;

    @SerializedName("only_right")
    @JsonProperty("only_right")
    @NotEmpty
    Set<AbstractDifferingPropertyOutput> onlyRight;

    @SerializedName("common")
    @JsonProperty("common")
    @NotEmpty
    Set<AbstractDifferingPropertyOutput> common;

    @SerializedName("differing")
    @JsonProperty("differing")
    @NotEmpty
    Set<AbstractDifferingPropertyOutput> differing;

    public PropertiesDiffOutput(PropertiesDiff propertiesDiff) {
        this.onlyLeft = propertiesDiff.getOnlyLeft().stream()
                .map(AbstractDifferingPropertyOutput::nonDifferingFromPropertyVisitor)
                .collect(Collectors.toSet());
        this.onlyRight = propertiesDiff.getOnlyRight().stream()
                .map(AbstractDifferingPropertyOutput::nonDifferingFromPropertyVisitor)
                .collect(Collectors.toSet());
        this.common = propertiesDiff.getCommon().stream()
                .map(AbstractDifferingPropertyOutput::nonDifferingFromPropertyVisitor)
                .collect(Collectors.toSet());
        this.differing = AbstractDifferingPropertyOutput.fromAbstractDifferingProperties(propertiesDiff.getDifferingProperties());
    }

    static List<PropertiesDiffOutput> fromPropertiesDiffs(List<PropertiesDiff> propertiesDiffs) {
        return Optional.ofNullable(propertiesDiffs)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(PropertiesDiffOutput::new)
                .collect(Collectors.toList());
    }

    static PropertiesDiffOutput onlyCommon(PropertyVisitorsSequence propertyVisitorsSequence) {
        return new PropertiesDiffOutput(
                Collections.emptySet(),
                Collections.emptySet(),
                propertyVisitorsSequence.stream()
                        .map(AbstractDifferingPropertyOutput::nonDifferingFromPropertyVisitor)
                        .collect(Collectors.toSet()),
                Collections.emptySet()
        );
    }
}
