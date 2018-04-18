package org.hesperides.domain.modules.commands;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.hesperides.domain.modules.TemplateCreatedEvent;
import org.hesperides.domain.modules.TemplateDeletedEvent;
import org.hesperides.domain.modules.TemplateUpdatedEvent;

public interface TemplateCommandsRepository {

    @EventSourcingHandler
    void on(TemplateCreatedEvent event);

    @EventSourcingHandler
    void on(TemplateUpdatedEvent event);

    @EventSourcingHandler
    void on(TemplateDeletedEvent event);

}
