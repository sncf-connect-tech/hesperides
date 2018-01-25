package org.hesperides.domain.modules.events;

import lombok.Value;
import org.hesperides.domain.modules.commands.Module;

@Value
public class ModuleCreatedEvent {
    Module.Key moduleKey;
}
