package org.hesperides.domain.modules.events;

import lombok.Value;
import org.hesperides.domain.modules.entities.Module;

@Value
public class TemplateDeletedEvent {
    Module.Key moduleKey;

    String templateName;
}
