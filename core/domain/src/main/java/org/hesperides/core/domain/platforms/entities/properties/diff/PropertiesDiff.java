package org.hesperides.core.domain.platforms.entities.properties.diff;

import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitor;

import java.util.Set;

@Value
public class PropertiesDiff {
    Set<PropertyVisitor> onlyLeft;
    Set<PropertyVisitor> onlyRight;
    Set<AbstractDifferingProperty> common;
    Set<AbstractDifferingProperty> differingProperties;
}
