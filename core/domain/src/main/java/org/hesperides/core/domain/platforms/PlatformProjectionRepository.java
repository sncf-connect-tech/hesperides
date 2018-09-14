package org.hesperides.core.domain.platforms;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;

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

    @EventHandler
    void onPlatformModulePropertiesUpdatedEvent(PlatformModulePropertiesUpdatedEvent event);

    @EventHandler
    void onPlatformPropertiesUpdatedEvent(PlatformPropertiesUpdatedEvent event);

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
    List<AbstractValuedPropertyView> onGetDeployedModulePropertiesQuery(GetDeployedModulesPropertiesQuery query);

    @QueryHandler
    List<ValuedPropertyView> onGetGlobalPropertiesQuery(GetGlobalPropertiesQuery query);
}
