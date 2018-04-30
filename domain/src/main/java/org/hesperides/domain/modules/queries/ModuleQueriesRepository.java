package org.hesperides.domain.modules.queries;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.*;

import java.util.List;
import java.util.Optional;

/**
 * stock et query les modules
 */
public interface ModuleQueriesRepository {
    @QueryHandler
    Optional<ModuleView> query(GetModuleByKeyQuery query);

    @QueryHandler
    List<String> query(GetModulesNamesQuery query);

    @QueryHandler
    List<String> query(GetModuleTypesQuery query);

    @QueryHandler
    List<String> query(GetModuleVersionsQuery query);

    @QueryHandler
    Boolean query(ModuleAlreadyExistsQuery query);

    @QueryHandler
    List<ModuleView> query(SearchModulesQuery query);
}
