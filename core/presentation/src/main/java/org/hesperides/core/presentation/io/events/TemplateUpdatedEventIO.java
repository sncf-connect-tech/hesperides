package org.hesperides.core.presentation.io.events;

import lombok.Value;
import org.hesperides.core.domain.modules.TemplateUpdatedEvent;
import org.hesperides.core.domain.templatecontainers.entities.Template;

@Value
public class TemplateUpdatedEventIO {
    private String moduleName;
    private String moduleVersion;
    private Template updated;  // only field used by legacy front is .name

    public TemplateUpdatedEventIO(TemplateUpdatedEvent templateUpdatedEvent) {
        this.updated = templateUpdatedEvent.getTemplate();
        this.moduleName = this.updated.getTemplateContainerKey().getName();
        this.moduleVersion = this.updated.getTemplateContainerKey().getVersion();
    }
}
