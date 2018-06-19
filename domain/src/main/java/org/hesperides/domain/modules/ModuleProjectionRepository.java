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
    void on(ModuleCreatedEvent event);

    @EventSourcingHandler
    void on(ModuleTechnosUpdatedEvent event);

    @EventSourcingHandler
    void on(ModuleDeletedEvent event);

    /*** QUERY HANDLERS ***/

    @QueryHandler
    Optional<ModuleView> query(GetModuleByKeyQuery query);

    @QueryHandler
    List<String> query(GetModulesNamesQuery query);

    @QueryHandler
    List<String> query(GetModuleVersionTypesQuery query);

    @QueryHandler
    List<String> query(GetModuleVersionsQuery query);

    @QueryHandler
    Boolean query(ModuleAlreadyExistsQuery query);

    @QueryHandler
    List<ModuleView> query(SearchModulesQuery query);

    @QueryHandler
    List<AbstractPropertyView> query(GetModulePropertiesQuery query);
}
