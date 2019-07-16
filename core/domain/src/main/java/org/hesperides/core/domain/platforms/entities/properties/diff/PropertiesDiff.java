package org.hesperides.core.domain.platforms.entities.properties.diff;

import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;

import java.util.Set;

@Value
public class PropertiesDiff {
    Set<AbstractValuedProperty> onlyLeft;
    Set<AbstractValuedProperty> onlyRight;
    Set<AbstractValuedProperty> common;
    Set<AbstractDifferingProperty> differingProperties;
}
