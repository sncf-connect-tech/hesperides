package org.hesperides.core.domain.platforms.entities.properties.diff;

import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public abstract class AbstractDifferingProperty {

    String name;

}
