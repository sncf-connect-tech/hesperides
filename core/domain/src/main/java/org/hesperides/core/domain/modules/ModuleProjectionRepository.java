package org.hesperides.core.domain.modules;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;

import java.util.List;
import java.util.Optional;

public interface ModuleProjectionRepository {

    /*** EVENT HANDLERS ***/

    @EventHandler
    void onModuleCreatedEvent(ModuleCreatedEvent event);

    @EventHandler
    void onModuleTechnosUpdatedEvent(ModuleTechnosUpdatedEvent event);

    @EventHandler
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
