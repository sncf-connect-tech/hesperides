package org.hesperides.domain.modules;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class CopyModuleCommand {

    @TargetAggregateIdentifier
    Module.Key moduleKey;

    Module.Key sourceModuleKey;
}
