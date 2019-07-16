package org.hesperides.core.domain.platforms.entities.properties.diff;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@EqualsAndHashCode(callSuper = true)
public class SimpleDifferingProperty extends AbstractDifferingProperty {

    String left;
    String right;

    public SimpleDifferingProperty(String name, String left, String right) {
        super(name);
        this.left = left;
        this.right = right;
    }

}
