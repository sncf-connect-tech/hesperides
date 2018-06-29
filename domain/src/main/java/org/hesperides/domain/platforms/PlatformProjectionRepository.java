package org.hesperides.domain.platforms;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.platforms.queries.views.PlatformView;
import org.hesperides.domain.platforms.queries.views.SearchPlatformView;

import java.util.List;
import java.util.Optional;

public interface PlatformProjectionRepository {

    /*** EVENT HANDLERS ***/

    @EventHandler
    void on(PlatformCreatedEvent event);

    @EventHandler
    void on(PlatformDeletedEvent event);

    /*** QUERY HANDLERS ***/

    @QueryHandler
    Optional<PlatformView> onGetPlatformByKeyQuery(GetPlatformByKeyQuery query);

    @QueryHandler
    public List<SearchPlatformView> onSearchPlatformQuery(SearchPlatformQuery query);
}
