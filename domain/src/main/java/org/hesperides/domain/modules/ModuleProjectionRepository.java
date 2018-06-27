package org.hesperides.domain.modules;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainers.queries.AbstractPropertyView;

import java.util.List;
import java.util.Optional;

public interface ModuleProjectionRepository {

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    void onModuleCreatedEvent(ModuleCreatedEvent event);

    @EventSourcingHandler
    void onModuleTechnosUpdatedEvent(ModuleTechnosUpdatedEvent event);

    @EventSourcingHandler
    void onModuleDeletedEvent(ModuleDeletedEvent event);

    /*** QUERY HANDLERS ***/

    @QueryHandler
    Optional<ModuleView> onGetModuleByKeyQuery(GetModuleByKeyQuery query);

    @QueryHandler
    List<String> onGetModulesNamesQuery(GetModulesNamesQuery query);

    @QueryHandler
    List<String> onGetModuleVersionTypesQuery(GetModuleVersionTypesQuery query);

    @QueryHandler
    List<String> onGetModuleVersionsQuery(GetModuleVersionsQuery query);

    @QueryHandler
    Boolean onModuleAlreadyExistsQuery(ModuleAlreadyExistsQuery query);

    @QueryHandler
    List<ModuleView> onSearchModulesQuery(SearchModulesQuery query);

    @QueryHandler
    List<AbstractPropertyView> onGetModulePropertiesQuery(GetModulePropertiesQuery query);
}
