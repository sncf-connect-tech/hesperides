package org.hesperides.domain.platforms;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.platforms.queries.views.ApplicationSearchView;
import org.hesperides.domain.platforms.queries.views.ApplicationView;
import org.hesperides.domain.platforms.queries.views.ModulePlatformView;
import org.hesperides.domain.platforms.queries.views.PlatformView;

import java.util.List;
import java.util.Optional;

public interface PlatformProjectionRepository {

    /*** EVENT HANDLERS ***/

    @EventHandler
    void onCreate(PlatformCreatedEvent event);

    @EventHandler
    void onDelete(PlatformDeletedEvent event);

    @EventHandler
    void onUpdate(PlatformUpdatedEvent event);

    /*** QUERY HANDLERS ***/

    @QueryHandler
    Optional<PlatformView> onGetPlatformByKeyQuery(GetPlatformByKeyQuery query);

    @QueryHandler
    Optional<ApplicationView> onGetApplicationByNameQuery(GetApplicationByNameQuery query);

    @QueryHandler
    List<ModulePlatformView> onGetPlatformUsingModuleQuery(GetPlatformsUsingModuleQuery query);

    @QueryHandler
    List<ApplicationSearchView> onSearchApplicationsByNameQuery(SearchApplicationsByNameQuery query);
}
