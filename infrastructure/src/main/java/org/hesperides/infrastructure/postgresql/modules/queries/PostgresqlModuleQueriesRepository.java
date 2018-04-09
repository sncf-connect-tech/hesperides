package org.hesperides.infrastructure.postgresql.modules.queries;

import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.queries.ModuleQueriesRepository;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.infrastructure.postgresql.modules.PostgresqlModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Profile("postgresql")
@Component
public class PostgresqlModuleQueriesRepository implements ModuleQueriesRepository {

    private final PostgresqlModuleRepository postgresqlModuleRepository;

    @Autowired
    public PostgresqlModuleQueriesRepository(PostgresqlModuleRepository postgresqlModuleRepository) {
        this.postgresqlModuleRepository = postgresqlModuleRepository;
    }

    @Override
    public Optional<ModuleView> query(ModuleByIdQuery query) {
        return Optional.empty();
    }

    @Override
    public List<String> queryAllModuleNames(ModulesNamesQuery query) {
        return null;
    }

    @Override
    public List<String> queryModuleTypes(ModuleTypesQuery query) {
        return null;
    }

    @Override
    public List<String> queryModuleVersions(ModuleVersionsQuery query) {
        return null;
    }

    @Override
    public Boolean query(ModuleAlreadyExistsQuery query) {
        return null;
    }

}
