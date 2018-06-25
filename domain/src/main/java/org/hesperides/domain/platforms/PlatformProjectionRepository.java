package org.hesperides.domain.platforms;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.platforms.queries.views.PlatformView;

import java.util.Optional;

public interface PlatformProjectionRepository {

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    void on(PlatformCreatedEvent event);

    /*** QUERY HANDLERS ***/

    @QueryHandler
    Optional<PlatformView> query(GetPlatformByKeyQuery query);
}
