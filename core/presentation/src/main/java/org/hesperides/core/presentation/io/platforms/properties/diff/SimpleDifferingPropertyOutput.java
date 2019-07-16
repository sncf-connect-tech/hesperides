package org.hesperides.core.presentation.io.platforms.properties.diff;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.diff.SimpleDifferingProperty;

@Value
@EqualsAndHashCode(callSuper = true)
public class SimpleDifferingPropertyOutput extends AbstractDifferingPropertyOutput {

    String left;
    String right;

    public SimpleDifferingPropertyOutput(String name, String left, String right) {
        super(name);
        this.left = left;
        this.right = right;
    }

    public SimpleDifferingPropertyOutput(SimpleDifferingProperty simpleDifferingProperty) {
        this(simpleDifferingProperty.getName(), simpleDifferingProperty.getLeft(), simpleDifferingProperty.getRight());
    }
}
