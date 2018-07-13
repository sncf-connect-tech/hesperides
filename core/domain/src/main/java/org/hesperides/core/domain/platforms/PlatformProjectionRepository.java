package org.hesperides.core.domain.platforms;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.platforms.queries.views.*;

import java.util.List;
import java.util.Optional;

public interface PlatformProjectionRepository {

    /*** EVENT HANDLERS ***/

    @EventHandler
    void onPlatformCreatedEvent(PlatformCreatedEvent event);

    @EventHandler
    void onPlatformDeletedEvent(PlatformDeletedEvent event);

    @EventHandler
    void onPlatformUpdatedEvent(PlatformUpdatedEvent event);

    /*** QUERY HANDLERS ***/

    @QueryHandler
    Optional<PlatformView> onGetPlatformByKeyQuery(GetPlatformByKeyQuery query);

    @QueryHandler
    Optional<ApplicationView> onGetApplicationByNameQuery(GetApplicationByNameQuery query);

    @QueryHandler
    List<ModulePlatformView> onGetPlatformUsingModuleQuery(GetPlatformsUsingModuleQuery query);

    @QueryHandler
    List<SearchPlatformResultView> onSearchPlatformsQuery(SearchPlatformsQuery query);

    @QueryHandler
    List<SearchApplicationResultView> onSearchApplicationsQuery(SearchApplicationsQuery query);
}
