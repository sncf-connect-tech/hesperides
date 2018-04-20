package org.hesperides.domain.modules.commands;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.hesperides.domain.modules.ModuleCreatedEvent;
import org.hesperides.domain.modules.ModuleDeletedEvent;
import org.hesperides.domain.modules.ModuleUpdatedEvent;

public interface ModuleCommandsRepository {

    @EventSourcingHandler
    void on(ModuleCreatedEvent event);

    @EventSourcingHandler
    void on(ModuleUpdatedEvent event);

    @EventSourcingHandler
    void on(ModuleDeletedEvent event);

}
