package org.hesperides.domain.modules.events;

import lombok.Value;
import org.hesperides.domain.modules.Module;

@Value
public class ModuleCreatedEvent {
    Module.Key moduleKey;
}
