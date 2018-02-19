package org.hesperides.domain.modules.queries;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.ModuleAlreadyExistsQuery;
import org.hesperides.domain.modules.ModuleByIdQuery;
import org.hesperides.domain.modules.ModulesNamesQuery;

import java.util.List;
import java.util.Optional;

/**
 * stock et query les modules
 */
public interface ModuleRepository {
    @QueryHandler
    Optional<ModuleView> query(ModuleByIdQuery query);

    @QueryHandler
    List<String> queryAllModuleNames(ModulesNamesQuery query);

    @QueryHandler
    Boolean query(ModuleAlreadyExistsQuery query);
}
