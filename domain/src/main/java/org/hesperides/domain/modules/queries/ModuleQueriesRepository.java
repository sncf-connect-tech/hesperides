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
    Optional<ModuleView> query(ModuleByIdQuery query);

    @QueryHandler
    List<String> queryAllModuleNames(ModulesNamesQuery query);

    @QueryHandler
    List<String> queryModuleTypes(ModuleTypesQuery query);

    @QueryHandler
    List<String> queryModuleVersions(ModuleVersionsQuery query);

    @QueryHandler
    Boolean query(ModuleAlreadyExistsQuery query);
}
