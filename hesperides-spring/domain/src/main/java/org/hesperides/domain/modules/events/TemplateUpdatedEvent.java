package org.hesperides.domain.modules.events;

import lombok.Value;
import org.hesperides.domain.modules.Module;
import org.hesperides.domain.modules.Template;

@Value
public class TemplateUpdatedEvent {
    Module.Key moduleKey;

    Template template;
}
