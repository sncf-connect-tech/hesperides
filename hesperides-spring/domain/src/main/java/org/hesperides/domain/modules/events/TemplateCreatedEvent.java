package org.hesperides.domain.modules.events;

import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.hesperides.domain.modules.queries.TemplateView;

@Value
public class TemplateCreatedEvent {
    Module.Key moduleKey;
    Template template;

    public TemplateView buildTemplateView() {
        return new TemplateView(template.getName(),
                moduleKey.getNamespace(),
                template.getFilename(),
                template.getFilename());
    }
}
