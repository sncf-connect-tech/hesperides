package org.hesperides.infrastructure.mongo.modules.queries;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleQueriesRepository;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.infrastructure.mongo.modules.MongoModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Profile("mongo")
@Component
public class MongoModuleQueriesRepository implements ModuleQueriesRepository {

    private final MongoModuleRepository mongoModuleRepository;

    @Autowired
    public MongoModuleQueriesRepository(MongoModuleRepository mongoModuleRepository) {
        this.mongoModuleRepository = mongoModuleRepository;
    }

    @QueryHandler
    @Override
    public Optional<ModuleView> query(ModuleByIdQuery query) {
        ModuleDocument moduleDocument = new ModuleDocument();
        moduleDocument.setName(query.getModuleKey().getName());
        moduleDocument.setVersion(query.getModuleKey().getVersion());
        moduleDocument.setVersionType(query.getModuleKey().getVersionType());
        moduleDocument = mongoModuleRepository.findOne(Example.of(moduleDocument));
        if (moduleDocument == null) {
            return Optional.empty();
        }
        return Optional.of(
                new ModuleView(
                        moduleDocument.getName(),
                        moduleDocument.getVersion(),
                        moduleDocument.getVersionType() == Module.Type.workingcopy,
                        moduleDocument.getVersionId()
                )
        );
    }

    @QueryHandler
    @Override
    public List<String> queryAllModuleNames(ModulesNamesQuery query) {
        return mongoModuleRepository.findAll()
                .stream()
                .map(ModuleDocument::getName)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> queryModuleTypes(ModuleTypesQuery query) {
        ModuleDocument moduleDocument = new ModuleDocument();
        moduleDocument.setName(query.getModuleName());
        moduleDocument.setVersion(query.getModuleVersion());
        return mongoModuleRepository.findAll(Example.of(moduleDocument))
                .stream()
                .map(ModuleDocument::getVersionType)
                .map(Module.Type::toString)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> queryModuleVersions(ModuleVersionsQuery query) {
        ModuleDocument moduleDocument = new ModuleDocument();
        moduleDocument.setName(query.getModuleName());
        return mongoModuleRepository.findAll(Example.of(moduleDocument))
                .stream()
                .map(ModuleDocument::getVersion)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public Boolean query(ModuleAlreadyExistsQuery query) {
        ModuleDocument moduleDocument = new ModuleDocument();
        moduleDocument.setName(query.getModuleKey().getName());
        moduleDocument.setVersion(query.getModuleKey().getVersion());
        moduleDocument.setVersionType(query.getModuleKey().getVersionType());
        return mongoModuleRepository.exists(Example.of(moduleDocument));
    }

}
