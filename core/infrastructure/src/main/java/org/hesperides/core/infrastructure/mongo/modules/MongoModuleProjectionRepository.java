package org.hesperides.core.infrastructure.mongo.modules;

import com.mongodb.client.DistinctIterable;
import io.micrometer.core.annotation.Timed;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.commons.SpringProfiles;
import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.modules.*;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.queries.ModulePropertiesView;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateContainerKeyView;
import org.hesperides.core.infrastructure.mongo.MongoConfiguration;
import org.hesperides.core.infrastructure.mongo.technos.MongoTechnoProjectionRepository;
import org.hesperides.core.infrastructure.mongo.technos.TechnoDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.KeyDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hesperides.commons.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.SpringProfiles.MONGO;
import static org.hesperides.core.infrastructure.mongo.Collections.MODULE;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoModuleProjectionRepository implements ModuleProjectionRepository {

    private final MongoModuleRepository moduleRepository;
    private final MongoTechnoProjectionRepository technoProjectionRepository;
    private final MongoTemplate mongoTemplate;
    private final SpringProfiles springProfiles;

    @Autowired
    public MongoModuleProjectionRepository(MongoModuleRepository moduleRepository,
                                           MongoTechnoProjectionRepository technoProjectionRepository,
                                           MongoTemplate mongoTemplate,
                                           SpringProfiles springProfiles) {
        this.moduleRepository = moduleRepository;
        this.technoProjectionRepository = technoProjectionRepository;
        this.mongoTemplate = mongoTemplate;
        this.springProfiles = springProfiles;
    }

    @PostConstruct
    private void ensureIndexCaseInsensitivity() {
        if (springProfiles.isActive(MONGO)) {
            MongoConfiguration.ensureCaseInsensitivity(mongoTemplate, MODULE);
        }
    }

    /*** EVENT HANDLERS ***/

    @EventHandler
    @Override
    @Timed
    public void onModuleCreatedEvent(ModuleCreatedEvent event) {
        List<TechnoDocument> technoDocuments = technoProjectionRepository.getTechnoDocumentsFromDomainInstances(event.getModule().getTechnos(), event.getModule().getKey());
        ModuleDocument moduleDocument = new ModuleDocument(event.getModuleId(), event.getModule(), technoDocuments);

        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @EventHandler
    @Override
    @Timed
    public void onModuleTechnosUpdatedEvent(ModuleTechnosUpdatedEvent event) {
        Optional<ModuleDocument> optModuleDocument = moduleRepository.findById(event.getModuleId());
        if (!optModuleDocument.isPresent()) {
            throw new NotFoundException("Module not found - update impossible - module ID: " + event.getModuleId());
        }
        ModuleDocument moduleDocument = optModuleDocument.get();
        List<TechnoDocument> technoDocuments = technoProjectionRepository.getTechnoDocumentsFromDomainInstances(
                event.getTechnos(), moduleDocument.getDomainKey());
        moduleDocument.setTechnos(technoDocuments);
        moduleDocument.setVersionId(event.getVersionId());

        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @EventHandler
    @Override
    @Timed
    public void onModuleDeletedEvent(ModuleDeletedEvent event) {
        moduleRepository.deleteById(event.getModuleId());
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    @Timed
    public Optional<String> onGetModuleIdFromKeyQuery(GetModuleIdFromKeyQuery query) {
        KeyDocument moduleKey = new KeyDocument(query.getModuleKey());
        return moduleRepository
                .findOptionalIdByKey(moduleKey)
                .map(ModuleDocument::getId);
    }

    @QueryHandler
    @Override
    @Timed
    public Optional<ModuleView> onGetModuleByIdQuery(GetModuleByIdQuery query) {
        return moduleRepository.findOptionalById(query.getModuleId())
                .map(ModuleDocument::toModuleView);
    }

    @QueryHandler
    @Override
    @Timed
    public Optional<ModuleView> onGetModuleByKeyQuery(GetModuleByKeyQuery query) {
        KeyDocument moduleKey = new KeyDocument(query.getModuleKey());

        return moduleRepository.findOptionalByKey(moduleKey)
                .map(ModuleDocument::toModuleView);
    }

    @QueryHandler
    @Override
    @Timed
    public Boolean onModuleExistsQuery(ModuleExistsQuery query) {
        KeyDocument moduleKey = new KeyDocument(query.getModuleKey());
        return moduleRepository.existsByKey(moduleKey);
    }

    @QueryHandler
    @Override
    @Timed
    public List<String> onGetModulesNameQuery(GetModulesNameQuery query) {
        final DistinctIterable<String> iterable = mongoTemplate.getCollection(MODULE).distinct("key.name", String.class);
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
    public List<String> onGetModuleVersionTypesQuery(GetModuleVersionTypesQuery query) {
        return moduleRepository.findKeysByNameAndVersion(query.getModuleName(), query.getModuleVersion())
                .stream()
                .map(ModuleDocument::getKey)
                .map(KeyDocument::isWorkingCopy)
                .map(TemplateContainer.VersionType::toString)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
    public List<String> onGetModuleVersionsQuery(GetModuleVersionsQuery query) {
        return moduleRepository.findVersionsByKeyName(query.getModuleName())
                .stream()
                .map(ModuleDocument::getKey)
                .map(KeyDocument::getVersion)
                .distinct()
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
    public List<ModuleView> onSearchModulesQuery(SearchModulesQuery query) {
        String[] values = query.getInput().split(" ");
        String name = values.length > 0 ? values[0] : "";
        String version = values.length > 1 ? values[1] : "";

        PageRequest pageable = PageRequest.of(0, query.getSize());
        return moduleRepository.findAllByKeyNameLikeAndKeyVersionLike(name, version, pageable)
                .stream()
                .map(ModuleDocument::toModuleView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
    public List<AbstractPropertyView> onGetModulePropertiesQuery(GetModulePropertiesQuery query) {
        KeyDocument moduleKey = new KeyDocument(query.getModuleKey());
        return moduleRepository.findPropertiesByModuleKey(moduleKey)
                .map(ModuleDocument::getProperties)
                .map(AbstractPropertyDocument::toViews)
                .orElseGet(Collections::emptyList);
    }

    @QueryHandler
    @Override
    @Timed
    public List<ModulePropertiesView> onGetModulesSimplePropertiesQuery(GetModulesPropertiesQuery query) {
        List<KeyDocument> modulesKeys = KeyDocument.fromModelKeys(query.getModulesKeys());

        return moduleRepository.findPropertiesByKeyIn(modulesKeys)
                .stream()
                .map(ModuleDocument::toModulePropertiesView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
    public List<TemplateContainerKeyView> onGetModulesUsingTechnoQuery(GetModulesUsingTechnoQuery query) {
        List<ModuleDocument> moduleDocuments = moduleRepository.findAllByTechnosId(query.getTechnoId());
        return moduleDocuments
                .stream()
                .map(ModuleDocument::getKey)
                .map(KeyDocument::toKeyView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
    public List<ModuleView> onGetModulesWithinQuery(GetModulesWithinQuery query) {
        List<KeyDocument> moduleKeys = KeyDocument.fromModelKeys(query.getModulesKeys());
        return moduleRepository.findModulesWithin(moduleKeys).stream()
                .map(ModuleDocument::toModuleView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
    public List<Module.Key> onGetModulesWithPasswordWithinQuery(GetModulesWithPasswordWithinQuery query) {
        List<KeyDocument> modulesKeys = KeyDocument.fromModelKeys(query.getModulesKeys());
        return moduleRepository.findModulesWithPasswordWithin(modulesKeys).stream()
                .map(ModuleDocument::getDomainKey)
                .collect(Collectors.toList());
    }
}
