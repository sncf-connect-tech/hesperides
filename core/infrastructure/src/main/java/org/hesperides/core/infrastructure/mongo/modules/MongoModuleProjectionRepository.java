package org.hesperides.core.infrastructure.mongo.modules;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.modules.*;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.infrastructure.mongo.technos.MongoTechnoProjectionRepository;
import org.hesperides.core.infrastructure.mongo.technos.TechnoDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.KeyDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;

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

    @EventHandler
    @Override
    public void onModuleCreatedEvent(ModuleCreatedEvent event) {
        List<TechnoDocument> technoDocuments = technoProjectionRepository.getTechnoDocumentsFromDomainInstances(event.getModule().getTechnos());
        ModuleDocument moduleDocument = new ModuleDocument(event.getModule(), technoDocuments);
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @EventHandler
    @Override
    public void onModuleTechnosUpdatedEvent(ModuleTechnosUpdatedEvent event) {
        KeyDocument keyDocument = new KeyDocument(event.getModuleKey());
        ModuleDocument moduleDocument = moduleRepository.findByKey(keyDocument);
        List<TechnoDocument> technoDocuments = technoProjectionRepository.getTechnoDocumentsFromDomainInstances(event.getTechnos());
        moduleDocument.setTechnos(technoDocuments);
        moduleDocument.setVersionId(event.getVersionId());
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @EventHandler
    @Override
    public void onModuleDeletedEvent(ModuleDeletedEvent event) {
        KeyDocument keyDocument = new KeyDocument(event.getModuleKey());
        moduleRepository.deleteByKey(keyDocument);
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    public Optional<ModuleView> onGetModuleByKeyQuery(GetModuleByKeyQuery query) {
        Optional<ModuleView> optionalModuleView = Optional.empty();
        KeyDocument keyDocument = new KeyDocument(query.getModuleKey());
        Optional<ModuleDocument> optionalModuleDocument = moduleRepository.findOptionalByKey(keyDocument);
        if (optionalModuleDocument.isPresent()) {
            optionalModuleView = Optional.of(optionalModuleDocument.get().toModuleView());
        }
        return optionalModuleView;
    }

    @QueryHandler
    @Override
    public List<String> onGetModulesNamesQuery(GetModulesNamesQuery query) {
        return mongoTemplate.getCollection("module").distinct("_id.name");
    }

    @QueryHandler
    @Override
    public List<String> onGetModuleVersionTypesQuery(GetModuleVersionTypesQuery query) {
        return moduleRepository.findByKeyNameAndKeyVersion(query.getModuleName(), query.getModuleVersion())
                .stream()
                .map(ModuleDocument::getKey)
                .map(KeyDocument::isWorkingCopy)
                .map(TemplateContainer.VersionType::toString)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> onGetModuleVersionsQuery(GetModuleVersionsQuery query) {
        return moduleRepository.findByKeyName(query.getModuleName())
                .stream()
                .map(ModuleDocument::getKey)
                .map(KeyDocument::getVersion)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public Boolean onModuleAlreadyExistsQuery(ModuleAlreadyExistsQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getModuleKey());
        Optional<ModuleDocument> optionalModuleDocument = moduleRepository.findOptionalByKey(keyDocument);
        return optionalModuleDocument.isPresent();
    }

    @QueryHandler
    @Override
    public List<ModuleView> onSearchModulesQuery(SearchModulesQuery query) {
        String[] values = query.getInput().split(" ");
        String name = values.length >= 1 ? values[0] : "";
        String version = values.length >= 2 ? values[1] : "";

        Pageable pageable = new PageRequest(0, 10); //TODO Sortir cette valeur dans le fichier de configuration
        List<ModuleDocument> moduleDocuments = moduleRepository.findAllByKeyNameLikeAndAndKeyVersionLike(name, version, pageable);
        return moduleDocuments.stream().map(ModuleDocument::toModuleView).collect(Collectors.toList());
    }

    @Override
    public List<AbstractPropertyView> onGetModulePropertiesQuery(GetModulePropertiesQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getModuleKey());
        ModuleDocument moduleDocument = moduleRepository.findByKey(keyDocument);
        return AbstractPropertyDocument.toAbstractPropertyViews(moduleDocument.getProperties());
    }
}
