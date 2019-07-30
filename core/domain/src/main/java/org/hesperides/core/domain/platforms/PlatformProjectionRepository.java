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

    @EventHandler
    PlatformView onRestoreDeletedPlatformEvent(RestoreDeletedPlatformEvent event);

    /*** QUERY HANDLERS ***/

    @QueryHandler
    Optional<String> onGetPlatformIdFromKeyQuery(GetPlatformIdFromKeyQuery query);

    @QueryHandler
    Optional<PlatformView> onGetPlatformByIdQuery(GetPlatformByIdQuery query);

    @QueryHandler
    Optional<String> onGetPlatformIdFromEvents(GetPlatformIdFromEvents query);

    @QueryHandler
    Optional<PlatformView> onGetPlatformByKeyQuery(GetPlatformByKeyQuery query);

    @QueryHandler
    PlatformView onGetPlatformAtPointInTimeQuery(GetPlatformAtPointInTimeQuery query);

    @QueryHandler
    Boolean onPlatformExistsByKeyQuery(PlatformExistsByKeyQuery query);

    @QueryHandler
    Optional<ApplicationView> onGetApplicationByNameQuery(GetApplicationByNameQuery query);

    @QueryHandler
    List<String> onGetInstancesModelQuery(GetInstancesModelQuery query);

    @QueryHandler
    List<ModulePlatformView> onGetPlatformUsingModuleQuery(GetPlatformsUsingModuleQuery query);

    @QueryHandler
    List<SearchApplicationResultView> onGetApplicationNamesQuery(GetApplicationNamesQuery query);

    @QueryHandler
    List<SearchApplicationResultView> onSearchApplicationsQuery(SearchApplicationsQuery query);

    @QueryHandler
    List<SearchPlatformResultView> onSearchPlatformsQuery(SearchPlatformsQuery query);

    @QueryHandler
    Long onGetPropertiesVersionIdQuery(GetPropertiesVersionIdQuery query);

    @QueryHandler
    List<AbstractValuedPropertyView> onGetDeployedModulePropertiesQuery(GetDeployedModulePropertiesQuery query);

    @QueryHandler
    Optional<Long> onGetGlobalPropertiesVersionIdQuery(GetGlobalPropertiesVersionIdQuery query);

    @QueryHandler
    List<ValuedPropertyView> onGetGlobalPropertiesQuery(GetGlobalPropertiesQuery query);

    @QueryHandler
    Boolean onDeployedModuleExistsQuery(DeployedModuleExistsQuery query);

    @QueryHandler
    Boolean onInstanceExistsQuery(InstanceExistsQuery query);

    @QueryHandler
    Boolean onApplicationExistsQuery(ApplicationExistsQuery query);

    @QueryHandler
    List<ApplicationView> onGetAllApplicationsDetailQuery(GetAllApplicationsDetailQuery query);
}
