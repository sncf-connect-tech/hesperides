package org.hesperides.domain.modules;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.queries.ModuleAlreadyExistsQuery;
import org.hesperides.domain.modules.queries.ModuleByIdQuery;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.modules.queries.ModulesNamesQuery;

import java.util.List;
import java.util.Optional;

/**
 * stock et query les modules
 */
public interface ModulesRepository {
    @QueryHandler
    Optional<ModuleView> query(ModuleByIdQuery query);

    @QueryHandler
    List<String> queryAllModuleNames(ModulesNamesQuery query);

    @QueryHandler
    Boolean query(ModuleAlreadyExistsQuery query);
}
