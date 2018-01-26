package org.hesperides.domain.modules.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import org.hesperides.domain.modules.Module;

@Value
public class CopyModuleCommand {

    @TargetAggregateIdentifier
    Module.Key moduleKey;

    Module.Key sourceModuleKey;
}
