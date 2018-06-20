package org.hesperides.domain.modules;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.templatecontainers.queries.TemplateView;

import java.util.List;
import java.util.Optional;

public interface TemplateProjectionRepository {

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    void on(TemplateCreatedEvent event);

    @EventSourcingHandler
    void on(TemplateUpdatedEvent event);

    @EventSourcingHandler
    void on(TemplateDeletedEvent event);

    /*** QUERY HANDLERS ***/

    @QueryHandler
    Optional<TemplateView> query(GetTemplateByNameQuery query);

    @QueryHandler
    List<TemplateView> query(GetModuleTemplatesQuery query);
}
