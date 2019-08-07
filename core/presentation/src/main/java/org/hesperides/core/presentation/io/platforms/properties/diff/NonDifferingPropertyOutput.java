package org.hesperides.core.presentation.io.platforms.properties.diff;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.visitors.SimplePropertyVisitor;

@Value
@EqualsAndHashCode(callSuper = true)
class NonDifferingPropertyOutput extends AbstractDifferingPropertyOutput {

    PropertyDiffValueOutput value;

    NonDifferingPropertyOutput(SimplePropertyVisitor propertyVisitor) {
        super(propertyVisitor.getName());
        this.value = new PropertyDiffValueOutput(propertyVisitor);
    }
}
