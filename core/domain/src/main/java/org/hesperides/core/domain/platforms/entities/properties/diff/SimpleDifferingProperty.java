package org.hesperides.core.domain.platforms.entities.properties.diff;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.visitors.SimplePropertyVisitor;
import org.springframework.lang.Nullable;

@Value
@EqualsAndHashCode(callSuper = true)
public class SimpleDifferingProperty extends AbstractDifferingProperty {

    @Nullable
    SimplePropertyVisitor left;
    @Nullable
    SimplePropertyVisitor right;

    SimpleDifferingProperty(String name, SimplePropertyVisitor left, SimplePropertyVisitor right) {
        super(name);
        this.left = left;
        this.right = right;
    }
}
