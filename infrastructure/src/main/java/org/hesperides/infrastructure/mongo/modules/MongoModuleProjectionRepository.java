package org.hesperides.infrastructure.mongo.modules;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.infrastructure.mongo.technos.MongoTechnoProjectionRepository;
import org.hesperides.infrastructure.mongo.technos.TechnoDocument;
import org.hesperides.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.infrastructure.mongo.templatecontainers.KeyDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.domain.framework.Profiles.FAKE_MONGO;
import static org.hesperides.domain.framework.Profiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoModuleProjectionRepository implements ModuleProjectionRepository {

    private final MongoModuleRepository moduleRepository;
    private final MongoTechnoProjectionRepository technoProjectionRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoModuleProjectionRepository(MongoModuleRepository moduleRepository,
                                           MongoTechnoProjectionRepository technoProjectionRepository,
                                           MongoTemplate mongoTemplate) {
        this.moduleRepository = moduleRepository;
        this.technoProjectionRepository = technoProjectionRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    @Override
    public void on(ModuleCreatedEvent event) {
        List<TechnoDocument> technoDocuments = technoProjectionRepository.getTechnoDocumentsFromDomainInstances(event.getModule().getTechnos());
        ModuleDocument moduleDocument = ModuleDocument.fromDomainInstance(event.getModule(), technoDocuments);
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleTechnosUpdatedEvent event) {
        ModuleDocument moduleDocument = moduleRepository.findByKey(KeyDocument.fromDomainInstance(event.getModuleKey()));
        List<TechnoDocument> technoDocuments = technoProjectionRepository.getTechnoDocumentsFromDomainInstances(event.getTechnos());
        moduleDocument.setTechnos(technoDocuments);
        moduleDocument.setVersionId(event.getVersionId());
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleDeletedEvent event) {
        moduleRepository.deleteByKey(KeyDocument.fromDomainInstance(event.getModuleKey()));
    }

    /*** QUERY HANDLERS ***/

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

    @Override
    public List<AbstractPropertyView> query(GetModulePropertiesQuery query) {
        ModuleDocument moduleDocument = moduleRepository.findByKey(KeyDocument.fromDomainInstance(query.getModuleKey()));
        return AbstractPropertyDocument.toAbstractPropertyViews(moduleDocument.getProperties());
    }
}
