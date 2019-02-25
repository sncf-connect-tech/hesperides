package org.hesperides.core.infrastructure.mongo.modules;

import com.mongodb.client.DistinctIterable;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.commons.spring.HasProfile;
import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.modules.*;
import org.hesperides.core.domain.modules.queries.ModuleSimplePropertiesView;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.modules.queries.TechnoModuleView;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.infrastructure.mongo.MongoSearchOptions;
import org.hesperides.core.infrastructure.mongo.eventstores.AxonEventRepository;
import org.hesperides.core.infrastructure.mongo.technos.MongoTechnoProjectionRepository;
import org.hesperides.core.infrastructure.mongo.technos.TechnoDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.KeyDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hesperides.commons.spring.HasProfile.isProfileActive;
import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;
import static org.hesperides.core.infrastructure.Constants.MODULE_COLLECTION_NAME;
import static org.hesperides.core.infrastructure.mongo.MongoSearchOptions.ensureCaseInsensitivity;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoModuleProjectionRepository implements ModuleProjectionRepository {

    private MongoModuleRepository moduleRepository;
    private final MongoTechnoProjectionRepository technoProjectionRepository;
    private final MongoTemplate mongoTemplate;
    private final MongoSearchOptions searchOptions;
    private final Environment environment;
    private final AxonEventRepository axonEventRepository;

    @Autowired
    public MongoModuleProjectionRepository(MongoModuleRepository moduleRepository,
                                           MongoTechnoProjectionRepository technoProjectionRepository,
                                           MongoTemplate mongoTemplate,
                                           MongoSearchOptions searchOptions,
                                           Environment environment,
                                           AxonEventRepository axonEventRepository) {
        this.moduleRepository = moduleRepository;
        this.technoProjectionRepository = technoProjectionRepository;
        this.mongoTemplate = mongoTemplate;
        this.searchOptions = searchOptions;
        this.environment = environment;
        this.axonEventRepository = axonEventRepository;
    }

    // Both only exist for batch:
    public MongoModuleRepository getMongoModuleRepository() {
        return moduleRepository;
    }
    public void setMongoModuleRepository(MongoModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    @PostConstruct
    private void ensureIndexCaseInsensitivity() {
        if (isProfileActive(environment, MONGO)) {
            ensureCaseInsensitivity(mongoTemplate, MODULE_COLLECTION_NAME);
        }
    }

    /*** EVENT HANDLERS ***/

    @EventHandler
    @Override
    public void onModuleCreatedEvent(ModuleCreatedEvent event) {
        List<TechnoDocument> technoDocuments = technoProjectionRepository.getTechnoDocumentsFromDomainInstances(event.getModule().getTechnos(), event.getModule().getKey());
        ModuleDocument moduleDocument = new ModuleDocument(event.getModuleId(), event.getModule(), technoDocuments);

        if (HasProfile.dataMigration()) {
            List<String> updatedTemplatesName = event.getModule().getTemplatesName();
            boolean isFirstEvent = axonEventRepository.hasOneEvent(event.getModuleId());
            moduleDocument.extractPropertiesAndSave(moduleRepository, updatedTemplatesName, isFirstEvent);
        } else {
            moduleDocument.extractPropertiesAndSave(moduleRepository, Collections.emptyList());
        }
    }

    @EventHandler
    @Override
    public void onModuleTechnosUpdatedEvent(ModuleTechnosUpdatedEvent event) {
        Optional<ModuleDocument> optModuleDocument = moduleRepository.findById(event.getModuleId());
        if (!optModuleDocument.isPresent()) {
            throw new NotFoundException("Module not found - update impossible - module ID: " + event.getModuleId());
        }
        ModuleDocument moduleDocument = optModuleDocument.get();
        List<TechnoDocument> technoDocuments = technoProjectionRepository.getTechnoDocumentsFromDomainInstances(event.getTechnos(), moduleDocument.getDomainKey());
        moduleDocument.setTechnos(technoDocuments);
        moduleDocument.setVersionId(event.getVersionId());

        if (HasProfile.dataMigration()) {
            List<String> updatedTemplatesName = Techno.getTemplatesName(event.getTechnos());
            moduleDocument.extractPropertiesAndSave(moduleRepository, updatedTemplatesName);
        } else {
            moduleDocument.extractPropertiesAndSave(moduleRepository, Collections.emptyList());
        }
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
        KeyDocument moduleKey = new KeyDocument(query.getModuleKey());
        return moduleRepository
                .findOptionalIdByKey(moduleKey)
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
        KeyDocument moduleKey = new KeyDocument(query.getModuleKey());

        return moduleRepository.findOptionalByKey(moduleKey)
                .map(ModuleDocument::toModuleView);
    }

    @QueryHandler
    @Override
    public Boolean onModuleExistsQuery(ModuleExistsQuery query) {
        KeyDocument moduleKey = new KeyDocument(query.getModuleKey());
        return moduleRepository.existsByKey(moduleKey);
    }

    @QueryHandler
    @Override
    public List<String> onGetModulesNameQuery(GetModulesNameQuery query) {
        final DistinctIterable<String> iterable = mongoTemplate.getCollection(MODULE_COLLECTION_NAME).distinct("key.name", String.class);
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }

    @QueryHandler
    @Override
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
    public List<String> onGetModuleVersionsQuery(GetModuleVersionsQuery query) {
        return moduleRepository.findVersionsByKeyName(query.getModuleName())
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

        Pageable pageable = PageRequest.of(0, searchOptions.getModuleSearchMaxResults());
        List<ModuleDocument> moduleDocuments = moduleRepository.findAllByKeyNameLikeAndKeyVersionLike(name, version, pageable);
        return moduleDocuments.stream().map(ModuleDocument::toModuleView).collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<AbstractPropertyView> onGetModulePropertiesQuery(GetModulePropertiesQuery query) {
        KeyDocument moduleKey = new KeyDocument(query.getModuleKey());
        return moduleRepository.findPropertiesByModuleKey(moduleKey)
                .map(ModuleDocument::getProperties)
                .map(AbstractPropertyDocument::toViews)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<ModuleSimplePropertiesView> onGetModulesSimplePropertiesQuery(GetModulesSimplePropertiesQuery query) {
        List<KeyDocument> modulesKeys = query.getModulesKeys()
                .stream()
                .map(KeyDocument::new)
                .collect(Collectors.toList());

        return moduleRepository.findPropertiesByKeyIn(modulesKeys)
                .stream()
                .map(ModuleDocument::toModuleSimplePropertiesView)
                .collect(Collectors.toList());
    }

    @Override
    public List<TechnoModuleView> onGetModulesUsingTechnoQuery(GetModulesUsingTechnoQuery query) {
        List<ModuleDocument> moduleDocuments = moduleRepository.findAllByTechnosId(query.getTechnoId());
        return moduleDocuments
                .stream()
                .map(ModuleDocument::getKey)
                .map(KeyDocument::toTechnoModuleView)
                .collect(Collectors.toList());
    }
}
