package org.hesperides.core.domain.workshopproperties;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.GetWorkshopPropertyByKeyQuery;
import org.hesperides.core.domain.WorkshopPropertyCreatedEvent;
import org.hesperides.core.domain.WorkshopPropertyExistsQuery;
import org.hesperides.core.domain.WorkshopPropertyUpdatedEvent;
import org.hesperides.core.domain.workshopproperties.queries.views.WorkshopPropertyView;

public interface WorkshopPropertyProjectionRepository {

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    void on(WorkshopPropertyCreatedEvent event);

    @EventSourcingHandler
    void on(WorkshopPropertyUpdatedEvent event);

    /*** QUERY HANDLERS ***/

    @QueryHandler
    Boolean query(WorkshopPropertyExistsQuery query);

    @QueryHandler
    WorkshopPropertyView query(GetWorkshopPropertyByKeyQuery query);
}
