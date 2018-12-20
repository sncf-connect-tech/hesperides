/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.core.infrastructure.mongo.technos;

import com.mongodb.client.DistinctIterable;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.technos.*;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.hesperides.core.infrastructure.mongo.MongoSearchOptions;
import org.hesperides.core.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.core.infrastructure.mongo.modules.MongoModuleRepository;
import org.hesperides.core.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.KeyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.TemplateDocument;
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
import static org.hesperides.core.infrastructure.Constants.TECHNO_COLLECTION_NAME;
import static org.hesperides.core.infrastructure.mongo.MongoSearchOptions.ensureCaseInsensitivity;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoTechnoProjectionRepository implements TechnoProjectionRepository {

    private final MongoTechnoRepository technoRepository;
    private final MongoModuleRepository moduleRepository;
    private final MongoTemplate mongoTemplate;
    private final MongoSearchOptions searchOptions;
    private final Environment environment;

    @Autowired
    public MongoTechnoProjectionRepository(MongoTechnoRepository technoRepository, MongoModuleRepository moduleRepository,
                                           MongoTemplate mongoTemplate, MongoSearchOptions searchOptions,
                                           Environment environment) {
        this.technoRepository = technoRepository;
        this.moduleRepository = moduleRepository;
        this.mongoTemplate = mongoTemplate;
        this.searchOptions = searchOptions;
        this.environment = environment;
    }

    @PostConstruct
    private void ensureIndexCaseInsensitivity() {
        if (isProfileActive(environment, MONGO)) {
            ensureCaseInsensitivity(mongoTemplate, TECHNO_COLLECTION_NAME);
        }
    }

    /*** EVENT HANDLERS ***/

    @EventHandler
    @Override
    public void onTechnoCreatedEvent(TechnoCreatedEvent event) {
        TechnoDocument technoDocument = new TechnoDocument(event.getTechnoId(), event.getTechno());
        technoDocument.extractPropertiesAndSave(technoRepository);
    }

    @Override
    public void onTechnoDeletedEvent(TechnoDeletedEvent event) {
        removeReferencesAndUpdateProperties(event.getTechnoId());
        technoRepository.deleteById(event.getTechnoId());
    }

    private void removeReferencesAndUpdateProperties(String technoId) {
        List<ModuleDocument> moduleDocuments = moduleRepository.findAllByTechnoId(technoId);
        moduleDocuments.forEach(moduleDocument -> {
            moduleDocument.setTechnos(
                    moduleDocument.getTechnos().stream()
                            .filter(technoDocument -> !technoDocument.getId().equals(technoId))
                            .collect(Collectors.toList()));
            moduleDocument.extractPropertiesAndSave(moduleRepository);
        });
    }

    @EventHandler
    @Override
    public void onTemplateAddedToTechnoEvent(TemplateAddedToTechnoEvent event) {
        Optional<TechnoDocument> optTechnoDocument = technoRepository.findById(event.getTechnoId());
        if (!optTechnoDocument.isPresent()) {
            throw new NotFoundException("Techno not found - template addition impossible - techno ID: " + event.getTechnoId());
        }
        TechnoDocument technoDocument = optTechnoDocument.get();
        TemplateDocument templateDocument = new TemplateDocument(event.getTemplate());
        technoDocument.addTemplate(templateDocument);
        technoDocument.extractPropertiesAndSave(technoRepository);
        updateModelUsingTechno(event.getTechnoId());
    }

    @EventHandler
    @Override
    public void onTechnoTemplateUpdatedEvent(TechnoTemplateUpdatedEvent event) {
        Optional<TechnoDocument> optTechnoDocument = technoRepository.findById(event.getTechnoId());
        if (!optTechnoDocument.isPresent()) {
            throw new NotFoundException("Techno not found - template update impossible - techno ID: " + event.getTechnoId());
        }
        TechnoDocument technoDocument = optTechnoDocument.get();
        TemplateDocument templateDocument = new TemplateDocument(event.getTemplate());
        technoDocument.updateTemplate(templateDocument);
        technoDocument.extractPropertiesAndSave(technoRepository);
        updateModelUsingTechno(event.getTechnoId());
    }

    @EventHandler
    @Override
    public void onTechnoTemplateDeletedEvent(TechnoTemplateDeletedEvent event) {
        Optional<TechnoDocument> optTechnoDocument = technoRepository.findById(event.getTechnoId());
        if (!optTechnoDocument.isPresent()) {
            throw new NotFoundException("Techno not found - template deletion impossible - techno ID: " + event.getTechnoId());
        }
        TechnoDocument technoDocument = optTechnoDocument.get();
        technoDocument.removeTemplate(event.getTemplateName());
        technoDocument.extractPropertiesAndSave(technoRepository);
        updateModelUsingTechno(event.getTechnoId());
    }

    /**
     * Met à jour le model des modules utilisant cette techno.
     * Cette logique devrait se trouver dans la couche application,
     * mais le batch de migration nécessite de l'avoir ici.
     */
    private void updateModelUsingTechno(String id) {
        List<ModuleDocument> moduleDocuments = moduleRepository.findAllByTechnoId(id);
        moduleDocuments.forEach(moduleDocument -> moduleDocument.extractPropertiesAndSave(moduleRepository));
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    public Optional<String> onGetTechnoIdFromKeyQuery(GetTechnoIdFromKeyQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        return technoRepository
                .findOptionalIdByKey(keyDocument)
                .map(TechnoDocument::getId);
    }

    @QueryHandler
    @Override
    public Boolean onTechnoExistsQuery(TechnoExistsQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        return technoRepository.existsByKey(keyDocument);
    }

    @QueryHandler
    @Override
    public List<String> onGetTechnosNameQuery(GetTechnosNameQuery query) {
        final DistinctIterable<String> iterable = mongoTemplate.getCollection(TECHNO_COLLECTION_NAME).distinct("key.name", String.class);
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> onGetTechnoVersionTypesQuery(GetTechnoVersionTypesQuery query) {
        return technoRepository.findKeysByNameAndVersion(query.getTechnoName(), query.getTechnoVersion())
                .stream()
                .map(TechnoDocument::getKey)
                .map(KeyDocument::isWorkingCopy)
                .map(TemplateContainer.VersionType::toString)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> onGetTechnoVersionsQuery(GetTechnoVersionsQuery query) {
        return technoRepository.findVersionsByKeyName(query.getTechnoName())
                .stream()
                .map(TechnoDocument::getKey)
                .map(KeyDocument::getVersion)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public Optional<TemplateView> onGetTemplateQuery(GetTemplateQuery query) {
        TemplateContainer.Key technoKey = query.getTechnoKey();
        return technoRepository.findTemplateByTechnoKeyAndTemplateName(new KeyDocument(technoKey), query.getTemplateName())
                .flatMap(technoDocument -> Optional.ofNullable(technoDocument.getTemplates())
                        .flatMap(templates -> templates.stream()
                                .filter(templateDocument -> templateDocument.getName().equalsIgnoreCase(query.getTemplateName()))
                                .findFirst()
                                .map(templateDocument -> templateDocument.toTemplateView(technoKey))));
    }

    @QueryHandler
    @Override
    public List<TemplateView> onGetTemplatesQuery(GetTemplatesQuery query) {
        TemplateContainer.Key technoKey = query.getTechnoKey();
        return technoRepository.findTemplatesByTechnoKey(new KeyDocument(technoKey))
                .map(technoDocument -> technoDocument.getTemplates()
                        .stream()
                        .map(templateDocument -> templateDocument.toTemplateView(technoKey))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @QueryHandler
    @Override
    public Optional<TechnoView> onGetTechnoQuery(GetTechnoQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        return technoRepository.findOptionalTechnoByKey(keyDocument)
                .map(TechnoDocument::toTechnoView);
    }

    @QueryHandler
    @Override
    public List<TechnoView> onSearchTechnosQuery(SearchTechnosQuery query) {
        String[] values = query.getInput().split(" ");
        String name = values.length >= 1 ? values[0] : "";
        String version = values.length >= 2 ? values[1] : "";

        Pageable pageable = PageRequest.of(0, searchOptions.getTechnoSearchMaxResults());
        return technoRepository.findAllByKeyNameLikeAndKeyVersionLike(name, version, pageable)
                .stream()
                .map(TechnoDocument::toTechnoView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<AbstractPropertyView> onGetTechnoPropertiesQuery(GetTechnoPropertiesQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        TechnoDocument technoDocument = technoRepository.findPropertiesByTechnoKey(keyDocument);
        return AbstractPropertyDocument.toAbstractPropertyViews(technoDocument.getProperties());
    }

    public List<TechnoDocument> getTechnoDocumentsFromDomainInstances(List<Techno> technos) {
        return technoRepository.findAllByKeyIn(Optional.ofNullable(technos)
                .orElse(Collections.emptyList())
                .stream()
                .map(techno -> new KeyDocument(techno.getKey()))
                .collect(Collectors.toList()));
    }
}
