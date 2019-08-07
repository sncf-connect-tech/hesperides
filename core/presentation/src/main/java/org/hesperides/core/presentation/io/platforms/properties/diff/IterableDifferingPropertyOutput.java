package org.hesperides.core.presentation.io.platforms.properties.diff;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.diff.IterableDifferingProperty;
import org.hesperides.core.domain.platforms.entities.properties.visitors.IterablePropertyVisitor;

import java.util.List;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(callSuper = true)
class IterableDifferingPropertyOutput extends AbstractDifferingPropertyOutput {

    List<PropertiesDiffOutput> items;

    IterableDifferingPropertyOutput(IterableDifferingProperty iterableDifferingProperty) {
        super(iterableDifferingProperty.getName());
        this.items = PropertiesDiffOutput.fromPropertiesDiffs(iterableDifferingProperty.getDifferingItems());
    }

    private IterableDifferingPropertyOutput(String name, List<PropertiesDiffOutput> items) {
        super(name);
        this.items = items;
    }

    static IterableDifferingPropertyOutput onlyCommon(IterablePropertyVisitor propertyVisitor) {
        return new IterableDifferingPropertyOutput(
                propertyVisitor.getName(),
                propertyVisitor.getItems().stream()
                        .map(PropertiesDiffOutput::onlyCommon)
                        .collect(Collectors.toList()));
    }
}
