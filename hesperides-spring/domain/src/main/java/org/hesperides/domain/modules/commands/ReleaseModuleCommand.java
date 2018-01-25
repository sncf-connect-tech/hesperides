package org.hesperides.domain.modules.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class ReleaseModuleCommand {

    @TargetAggregateIdentifier
    String name;

    String newVersion;
}
