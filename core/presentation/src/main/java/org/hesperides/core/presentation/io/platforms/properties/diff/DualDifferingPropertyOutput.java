package org.hesperides.core.presentation.io.platforms.properties.diff;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.diff.SimpleDifferingProperty;

@Value
@EqualsAndHashCode(callSuper = true)
public class DualDifferingPropertyOutput extends AbstractDifferingPropertyOutput {

    PropertyDiffValueOutput left;
    PropertyDiffValueOutput right;

    DualDifferingPropertyOutput(SimpleDifferingProperty simpleDifferingProperty) {
        super(simpleDifferingProperty.getName());
        this.left = new PropertyDiffValueOutput(simpleDifferingProperty.getLeft());
        this.right = new PropertyDiffValueOutput(simpleDifferingProperty.getRight());
    }
}
