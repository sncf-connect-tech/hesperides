package org.hesperides.domain.modules;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class CreateModuleCommand {

    @TargetAggregateIdentifier
    String name;
}
