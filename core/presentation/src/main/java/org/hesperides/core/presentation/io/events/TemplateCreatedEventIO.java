package org.hesperides.core.presentation.io.events;

import lombok.Value;
import org.hesperides.core.domain.modules.TemplateCreatedEvent;
import org.hesperides.core.domain.templatecontainers.entities.Template;

@Value
public class TemplateCreatedEventIO {
    private String moduleName;
    private String moduleVersion;
    private Template created;  // only field used by legacy front is .name

    public TemplateCreatedEventIO(TemplateCreatedEvent templateCreatedEvent) {
        this.created = templateCreatedEvent.getTemplate();
        this.moduleName = this.created.getTemplateContainerKey().getName();
        this.moduleVersion = this.created.getTemplateContainerKey().getVersion();
    }
}
