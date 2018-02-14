package org.hesperides.domain.modules.events;

import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;

@Value
public class TemplateUpdatedEvent {
    Module.Key moduleKey;
    Template template;
}
