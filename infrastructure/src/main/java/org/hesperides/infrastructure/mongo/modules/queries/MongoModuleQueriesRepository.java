package org.hesperides.infrastructure.mongo.modules.queries;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleQueriesRepository;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.infrastructure.mongo.modules.MongoModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public class MongoModuleQueriesRepository implements ModuleQueriesRepository {

    private final MongoModuleRepository repository;

    @Autowired
    public MongoModuleQueriesRepository(MongoModuleRepository repository) {
        this.repository = repository;
    }

    @QueryHandler
    @Override
    public Optional<ModuleView> query(GetModuleByKeyQuery query) {
        Optional<ModuleView> moduleView = Optional.empty();
        TemplateContainer.Key key = query.getModuleKey();
        ModuleDocument moduleDocument = repository.findByNameAndVersionAndWorkingCopy(key.getName(), key.getVersion(), key.isWorkingCopy());
        if (moduleDocument != null) {
            moduleView = Optional.of(moduleDocument.toModuleView());
        }
        return moduleView;
    }

    @QueryHandler
    @Override
    public List<String> query(GetModulesNamesQuery query) {
        return repository.findAll()
                .stream()
                .map(ModuleDocument::getName)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> query(GetModuleTypesQuery query) {
        return repository.findByNameAndVersion(query.getModuleName(), query.getModuleVersion())
                .stream()
                .map(ModuleDocument::isWorkingCopy)
                .map(isWorkingCopy -> Module.Type.toString(isWorkingCopy))
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> query(GetModuleVersionsQuery query) {
        return repository.findByName(query.getModuleName())
                .stream()
                .map(ModuleDocument::getVersion)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public Boolean query(ModuleAlreadyExistsQuery query) {
        TemplateContainer.Key key = query.getModuleKey();
        ModuleDocument moduleDocument = repository.findByNameAndVersionAndWorkingCopy(key.getName(), key.getVersion(), key.isWorkingCopy());
        return moduleDocument != null;
    }

}
