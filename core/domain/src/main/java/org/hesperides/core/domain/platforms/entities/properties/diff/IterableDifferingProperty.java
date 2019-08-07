package org.hesperides.core.domain.platforms.entities.properties.diff;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
public class IterableDifferingProperty extends AbstractDifferingProperty {

    List<PropertiesDiff> differingItems;

    IterableDifferingProperty(String name, List<PropertiesDiff> differingItems) {
        super(name);
        this.differingItems = differingItems;
    }
}
