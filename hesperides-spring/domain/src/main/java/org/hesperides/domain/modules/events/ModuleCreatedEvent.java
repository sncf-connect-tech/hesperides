package org.hesperides.domain.modules.events;

import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;

import java.util.List;

@Value
public class ModuleCreatedEvent implements ModuleEvent {
    Module.Key moduleKey;
}
