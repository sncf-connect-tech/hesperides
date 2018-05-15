package org.hesperides.infrastructure.mongo.modules.queries;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.queries.ModuleQueriesRepository;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.infrastructure.mongo.modules.MongoModuleRepository;
import org.hesperides.infrastructure.mongo.templatecontainer.KeyDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public class MongoModuleQueriesRepository implements ModuleQueriesRepository {

    private final MongoModuleRepository moduleRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoModuleQueriesRepository(MongoModuleRepository moduleRepository, MongoTemplate mongoTemplate) {
        this.moduleRepository = moduleRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @QueryHandler
    @Override
    public Optional<ModuleView> query(GetModuleByKeyQuery query) {
        Optional<ModuleView> moduleView = Optional.empty();
        ModuleDocument moduleDocument = moduleRepository.findByKey(KeyDocument.fromDomainInstance(query.getModuleKey()));
        if (moduleDocument != null) {
            moduleView = Optional.of(moduleDocument.toModuleView());
        }
        return moduleView;
    }

    @QueryHandler
    @Override
    public List<String> query(GetModulesNamesQuery query) {
        return mongoTemplate.getCollection("module").distinct("_id.name");
    }

    @QueryHandler
    @Override
    public List<String> query(GetModuleVersionTypesQuery query) {
        return moduleRepository.findByKeyNameAndKeyVersion(query.getModuleName(), query.getModuleVersion())
                .stream()
                .map(ModuleDocument::getKey)
                .map(KeyDocument::isWorkingCopy)
                .map(TemplateContainer.VersionType::toString)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> query(GetModuleVersionsQuery query) {
        return moduleRepository.findByKeyName(query.getModuleName())
                .stream()
                .map(ModuleDocument::getKey)
                .map(KeyDocument::getVersion)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public Boolean query(ModuleAlreadyExistsQuery query) {
        TemplateContainer.Key key = query.getModuleKey();
        ModuleDocument moduleDocument = moduleRepository.findByKey(KeyDocument.fromDomainInstance(key));
        return moduleDocument != null;
    }

    @QueryHandler
    @Override
    public List<ModuleView> query(SearchModulesQuery query) {
        String[] values = query.getInput().split(" ");
        String name = values.length >= 1 ? values[0] : "";
        String version = values.length >= 2 ? values[1] : "";

        Pageable pageableRequest = new PageRequest(0, 10); //TODO Sortir cette valeur dans le fichier de configuration
        List<ModuleDocument> moduleDocuments = moduleRepository.findAllByKeyNameLikeAndAndKeyVersionLike(name, version, pageableRequest);
        return moduleDocuments.stream().map(ModuleDocument::toModuleView).collect(Collectors.toList());
    }
}
