package org.hesperides.core.domain.modules;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;

import java.util.List;
import java.util.Optional;

public interface TemplateProjectionRepository {

    /*** EVENT HANDLERS ***/

    @EventHandler
    void onTemplateCreatedEvent(TemplateCreatedEvent event);

    @EventHandler
    void onTemplateUpdatedEvent(TemplateUpdatedEvent event);

    @EventHandler
    void onTemplateDeletedEvent(TemplateDeletedEvent event);

    /*** QUERY HANDLERS ***/

    @QueryHandler
    Optional<TemplateView> onGetTemplateByNameQuery(GetTemplateByNameQuery query);

    @QueryHandler
    List<TemplateView> onGetModuleTemplatesQuery(GetModuleTemplatesQuery query);
}
