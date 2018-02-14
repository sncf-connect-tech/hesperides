package org.hesperides.domain.modules.events;

import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;

@Value
public class TemplateCreatedEvent {
    Module.Key moduleKey;
    Template template;
}
