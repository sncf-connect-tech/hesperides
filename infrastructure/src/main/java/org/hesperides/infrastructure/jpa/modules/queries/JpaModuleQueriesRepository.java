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

@Profile("jpa")
@Component
public class JpaModuleQueriesRepository implements ModuleQueriesRepository {

    private final JpaModuleRepository jpaModuleRepository;

    @Autowired
    public JpaModuleQueriesRepository(JpaModuleRepository jpaModuleRepository) {
        this.jpaModuleRepository = jpaModuleRepository;
    }

    @QueryHandler
    @Override
    public Optional<ModuleView> query(ModuleByIdQuery query) {
        ModuleEntity.ModuleEntityId id = new ModuleEntity.ModuleEntityId(
                query.getModuleKey().getName(),
                query.getModuleKey().getVersion(),
                query.getModuleKey().getVersionType()
        );
        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity.setModuleEntityId(id);
        moduleEntity = jpaModuleRepository.findOne(id);
        if (moduleEntity == null) {
            return Optional.empty();
        }
        return Optional.of(
                new ModuleView(
                        moduleEntity.getModuleEntityId().getName(),
                        moduleEntity.getModuleEntityId().getVersion(),
                        moduleEntity.getModuleEntityId().getVersionType() == Module.Type.workingcopy,
                        moduleEntity.getVersionId()
                )
        );
    }

    @QueryHandler
    @Override
    public List<String> queryAllModuleNames(ModulesNamesQuery query) {
        return jpaModuleRepository.findAll()
                .stream()
                .map(ModuleEntity::getModuleEntityId)
                .map(ModuleEntity.ModuleEntityId::getName)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> queryModuleTypes(ModuleTypesQuery query) {
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
    public List<String> queryModuleVersions(ModuleVersionsQuery query) {
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

}
