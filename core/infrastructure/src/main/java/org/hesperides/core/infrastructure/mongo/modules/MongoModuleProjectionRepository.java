package org.hesperides.core.infrastructure.mongo.modules;

import com.mongodb.client.DistinctIterable;
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
import java.util.stream.StreamSupport;

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
        ModuleDocument moduleDocument = new ModuleDocument(event.getModuleId(), event.getModule(), technoDocuments);
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @EventHandler
    @Override
    public void onModuleTechnosUpdatedEvent(ModuleTechnosUpdatedEvent event) {
        ModuleDocument moduleDocument = moduleRepository.findById(event.getModuleId()).get();
        List<TechnoDocument> technoDocuments = technoProjectionRepository.getTechnoDocumentsFromDomainInstances(event.getTechnos());
        moduleDocument.setTechnos(technoDocuments);
        moduleDocument.setVersionId(event.getVersionId());
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @EventHandler
    @Override
    public void onModuleDeletedEvent(ModuleDeletedEvent event) {
        moduleRepository.deleteById(event.getModuleId());
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    public Optional<String> onGetModuleIdFromKeyQuery(GetModuleIdFromKeyQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getModuleKey());
        return moduleRepository
                .findOptionalIdByKey(keyDocument)
                .map(ModuleDocument::getId);
    }

    @QueryHandler
    @Override
    public Optional<ModuleView> onGetModuleByIdQuery(GetModuleByIdQuery query) {
        return moduleRepository.findOptionalById(query.getModuleId())
                .map(ModuleDocument::toModuleView);
    }

    @QueryHandler
    @Override
    public Optional<ModuleView> onGetModuleByKeyQuery(GetModuleByKeyQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getModuleKey());

        return moduleRepository.findOptionalByKey(keyDocument)
                .map(ModuleDocument::toModuleView);
    }

    @QueryHandler
    @Override
    public Boolean onModuleExistsQuery(ModuleExistsQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getModuleKey());
        return moduleRepository.existsByKey(keyDocument);
    }

    @QueryHandler
    @Override
    public List<String> onGetModulesNameQuery(GetModulesNameQuery query) {
        final DistinctIterable<String> iterable = mongoTemplate.getCollection("module").distinct("key.name", String.class);
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
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
    public List<ModuleView> onSearchModulesQuery(SearchModulesQuery query) {
        String[] values = query.getInput().split(" ");
        String name = values.length >= 1 ? values[0] : "";
        String version = values.length >= 2 ? values[1] : "";

        Pageable pageable = PageRequest.of(0, 10); //TODO Sortir cette valeur dans le fichier de configuration
        List<ModuleDocument> moduleDocuments = moduleRepository.findAllByKeyNameLikeAndKeyVersionLike(name, version, pageable);
        return moduleDocuments.stream().map(ModuleDocument::toModuleView).collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<AbstractPropertyView> onGetModulePropertiesQuery(GetModulePropertiesQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getModuleKey());
        ModuleDocument moduleDocument = moduleRepository.findByKey(keyDocument);
        return AbstractPropertyDocument.toAbstractPropertyViews(moduleDocument.getProperties());
    }
}
