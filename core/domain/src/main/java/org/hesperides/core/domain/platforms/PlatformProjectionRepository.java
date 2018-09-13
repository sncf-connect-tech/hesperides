package org.hesperides.core.domain.platforms;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.platforms.queries.views.ApplicationView;
import org.hesperides.core.domain.platforms.queries.views.ModulePlatformView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.platforms.queries.views.SearchApplicationResultView;
import org.hesperides.core.domain.platforms.queries.views.SearchPlatformResultView;
import org.hesperides.core.domain.platforms.queries.views.InstanceModelView;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;

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
    Boolean onPlatformExistsByKeyQuery(PlatformExistsByKeyQuery query);

    @QueryHandler
    Optional<PlatformView> onGetPlatformByKeyQuery(GetPlatformByKeyQuery query);

    @QueryHandler
    Optional<ApplicationView> onGetApplicationByNameQuery(GetApplicationByNameQuery query);

    @QueryHandler
    Optional<InstanceModelView> onGetInstanceModelQuery(GetInstanceModelQuery query);

    @QueryHandler
    List<ModulePlatformView> onGetPlatformUsingModuleQuery(GetPlatformsUsingModuleQuery query);

    @QueryHandler
    List<SearchPlatformResultView> onSearchPlatformsQuery(SearchPlatformsQuery query);

    @QueryHandler
    List<SearchApplicationResultView> onSearchApplicationsQuery(SearchApplicationsQuery query);

    @QueryHandler
    List<AbstractValuedPropertyView> onGetPropertiesQuery(GetPropertiesQuery query);
}
