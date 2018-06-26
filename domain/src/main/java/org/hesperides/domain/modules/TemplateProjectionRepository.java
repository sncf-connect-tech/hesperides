package org.hesperides.domain.modules;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.templatecontainers.queries.TemplateView;

import java.util.List;
import java.util.Optional;

public interface TemplateProjectionRepository {

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    void onTemplateCreatedEvent(TemplateCreatedEvent event);

    @EventSourcingHandler
    void onTemplateUpdatedEvent(TemplateUpdatedEvent event);

    @EventSourcingHandler
    void onTemplateDeletedEvent(TemplateDeletedEvent event);

    /*** QUERY HANDLERS ***/

    @QueryHandler
    Optional<TemplateView> onGetTemplateByNameQuery(GetTemplateByNameQuery query);

    @QueryHandler
    List<TemplateView> onGetModuleTemplatesQuery(GetModuleTemplatesQuery query);
}
