package org.hesperides.domain.modules.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class CreateModuleCommand {

    @TargetAggregateIdentifier
    Module.Key moduleKey;
}
