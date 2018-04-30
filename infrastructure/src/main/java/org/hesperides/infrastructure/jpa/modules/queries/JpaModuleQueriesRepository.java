package org.hesperides.infrastructure.jpa.modules.queries;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleQueriesRepository;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.infrastructure.jpa.modules.JpaModuleRepository;
import org.hesperides.infrastructure.jpa.modules.ModuleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.domain.Profiles.JPA;

@Profile(JPA)
@Component
public class JpaModuleQueriesRepository implements ModuleQueriesRepository {

    private final JpaModuleRepository jpaModuleRepository;

    @Autowired
    public JpaModuleQueriesRepository(JpaModuleRepository jpaModuleRepository) {
        this.jpaModuleRepository = jpaModuleRepository;
    }

    @QueryHandler
    @Override
    public Optional<ModuleView> query(GetModuleByKeyQuery query) {
        Optional<ModuleView> result = Optional.empty();
        ModuleEntity.ModuleEntityId id = new ModuleEntity.ModuleEntityId(
                query.getModuleKey().getName(),
                query.getModuleKey().getVersion(),
                query.getModuleKey().getVersionType()
        );
        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity.setModuleEntityId(id);
        moduleEntity = jpaModuleRepository.findOne(id);
        if (moduleEntity != null) {
            result = Optional.of(moduleEntity.toModuleView());
        }
        return result;
    }

    @QueryHandler
    @Override
    public List<String> query(GetModulesNamesQuery query) {
        return jpaModuleRepository.findAll()
                .stream()
                .map(ModuleEntity::getModuleEntityId)
                .map(ModuleEntity.ModuleEntityId::getName)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> query(GetModuleTypesQuery query) {
        ModuleEntity.ModuleEntityId id = new ModuleEntity.ModuleEntityId(
                query.getModuleName(),
                query.getModuleVersion(),
                null
        );
        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity.setModuleEntityId(id);
        return jpaModuleRepository.findAll(Example.of(moduleEntity))
                .stream()
                .map(ModuleEntity::getModuleEntityId)
                .map(ModuleEntity.ModuleEntityId::getVersionType)
                .map(Module.Type::toString)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> query(GetModuleVersionsQuery query) {
        ModuleEntity.ModuleEntityId id = new ModuleEntity.ModuleEntityId(
                query.getModuleName(),
                null,
                null
        );
        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity.setModuleEntityId(id);
        return jpaModuleRepository.findAll(Example.of(moduleEntity))
                .stream()
                .map(ModuleEntity::getModuleEntityId)
                .map(ModuleEntity.ModuleEntityId::getVersion)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public Boolean query(ModuleAlreadyExistsQuery query) {
        ModuleEntity.ModuleEntityId id = new ModuleEntity.ModuleEntityId(
                query.getModuleKey().getName(),
                query.getModuleKey().getVersion(),
                query.getModuleKey().getVersionType()
        );
        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity.setModuleEntityId(id);
        return jpaModuleRepository.exists(Example.of(moduleEntity));
    }

    @Override
    @QueryHandler
    public List<ModuleView> query(SearchModulesQuery query) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
