package org.hesperides.domain.workshopproperties;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.GetWorkshopPropertyByKeyQuery;
import org.hesperides.domain.WorkshopPropertyCreatedEvent;
import org.hesperides.domain.WorkshopPropertyExistsQuery;
import org.hesperides.domain.WorkshopPropertyUpdatedEvent;
import org.hesperides.domain.workshopproperties.queries.views.WorkshopPropertyView;

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
