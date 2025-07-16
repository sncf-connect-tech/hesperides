/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/sncf-connect-tech/hesperides)
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
import io.micrometer.core.annotation.Timed;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.commons.SpringProfiles;
import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.technos.*;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateContainerKeyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.hesperides.core.infrastructure.mongo.MongoConfiguration;
import org.hesperides.core.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.core.infrastructure.mongo.modules.MongoModuleRepository;
import org.hesperides.core.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.KeyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.TemplateDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hesperides.commons.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.SpringProfiles.MONGO;
import static org.hesperides.core.infrastructure.mongo.Collections.TECHNO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoTechnoProjectionRepository implements TechnoProjectionRepository {

    private final MongoTechnoRepository technoRepository;
    private final MongoModuleRepository moduleRepository;
    private final MongoTemplate mongoTemplate;
    private final SpringProfiles springProfiles;

    @Autowired
    public MongoTechnoProjectionRepository(MongoTechnoRepository technoRepository,
                                           MongoModuleRepository moduleRepository,
                                           MongoTemplate mongoTemplate,
                                           SpringProfiles springProfiles) {
        this.technoRepository = technoRepository;
        this.moduleRepository = moduleRepository;
        this.mongoTemplate = mongoTemplate;
        this.springProfiles = springProfiles;
    }

    @PostConstruct
    private void ensureIndexCaseInsensitivity() {
        if (springProfiles.isActive(MONGO)) {
            MongoConfiguration.ensureCaseInsensitivity(mongoTemplate, TECHNO);
        }
    }

    /*** EVENT HANDLERS ***/

    @EventHandler
    @Override
    @Timed
    public void onTechnoCreatedEvent(TechnoCreatedEvent event) {
        TechnoDocument technoDocument = new TechnoDocument(event.getTechnoId(), event.getTechno());
        technoDocument.extractPropertiesAndSave(technoRepository);
    }

    @EventHandler
    @Override
    @Timed
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
    @Timed
    public void onTemplateAddedToTechnoEvent(TemplateAddedToTechnoEvent event) {
        Optional<TechnoDocument> optTechnoDocument = technoRepository.findById(event.getTechnoId());
        if (!optTechnoDocument.isPresent()) {
            throw new NotFoundException("Techno not found - template addition impossible - techno ID: " + event.getTechnoId());
        }
        TechnoDocument technoDocument = optTechnoDocument.get();
        TemplateDocument templateDocument = new TemplateDocument(event.getTemplate());
        technoDocument.addTemplate(templateDocument);

        technoDocument.extractPropertiesAndSave(technoRepository);
        updateModelsUsingTechno(event.getTechnoId());
    }

    @EventHandler
    @Override
    @Timed
    public void onTechnoTemplateUpdatedEvent(TechnoTemplateUpdatedEvent event) {
        Optional<TechnoDocument> optTechnoDocument = technoRepository.findById(event.getTechnoId());
        if (!optTechnoDocument.isPresent()) {
            throw new NotFoundException("Techno not found - template update impossible - techno ID: " + event.getTechnoId());
        }
        TechnoDocument technoDocument = optTechnoDocument.get();
        TemplateDocument templateDocument = new TemplateDocument(event.getTemplate());
        technoDocument.updateTemplate(templateDocument);
        technoDocument.extractPropertiesAndSave(technoRepository);
        updateModelsUsingTechno(event.getTechnoId());
    }

    @EventHandler
    @Override
    @Timed
    public void onTechnoTemplateDeletedEvent(TechnoTemplateDeletedEvent event) {
        Optional<TechnoDocument> optTechnoDocument = technoRepository.findById(event.getTechnoId());
        if (!optTechnoDocument.isPresent()) {
            throw new NotFoundException("Techno not found - template deletion impossible - techno ID: " + event.getTechnoId());
        }
        TechnoDocument technoDocument = optTechnoDocument.get();
        technoDocument.removeTemplate(event.getTemplateName());
        technoDocument.extractPropertiesAndSave(technoRepository);
        updateModelsUsingTechno(event.getTechnoId());
    }

    /**
     * Met à jour le model des modules utilisant cette techno.
     * Cette logique devrait se trouver dans la couche application,
     * mais le batch de migration nécessite de l'avoir ici.
     */
    private void updateModelsUsingTechno(String technoId) {
        List<ModuleDocument> moduleDocuments = moduleRepository.findAllByTechnoId(technoId);
        moduleDocuments.forEach(moduleDocument -> moduleDocument.extractPropertiesAndSave(moduleRepository));
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    @Timed
    public Optional<String> onGetTechnoIdFromKeyQuery(GetTechnoIdFromKeyQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        return technoRepository
                .findOptionalIdByKey(keyDocument)
                .map(TechnoDocument::getId);
    }

    @QueryHandler
    @Override
    @Timed
    public Boolean onTechnoExistsQuery(TechnoExistsQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        return technoRepository.existsByKey(keyDocument);
    }

    @QueryHandler
    @Override
    @Timed
    public List<String> onGetTechnosNameQuery(GetTechnosNameQuery query) {
        final DistinctIterable<String> iterable = mongoTemplate.getCollection(TECHNO).distinct("key.name", String.class);
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
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
    @Timed
    public List<String> onGetTechnoVersionsQuery(GetTechnoVersionsQuery query) {
        return technoRepository.findVersionsByKeyName(query.getTechnoName())
                .stream()
                .map(TechnoDocument::getKey)
                .map(KeyDocument::getVersion)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
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
    @Timed
    public List<TemplateView> onGetTemplatesQuery(GetTemplatesQuery query) {
        TemplateContainer.Key technoKey = query.getTechnoKey();
        return technoRepository.findTemplatesByTechnoKey(new KeyDocument(technoKey))
                .map(technoDocument -> technoDocument.getTemplates()
                        .stream()
                        .map(templateDocument -> templateDocument.toTemplateView(technoKey))
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    @QueryHandler
    @Override
    @Timed
    public Optional<TechnoView> onGetTechnoQuery(GetTechnoQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        return technoRepository.findOptionalTechnoByKey(keyDocument)
                .map(TechnoDocument::toTechnoView);
    }

    @QueryHandler
    @Override
    @Timed
    public List<TechnoView> onSearchTechnosQuery(SearchTechnosQuery query) {
        String[] values = query.getInput().split(" ");
        String name = values.length >= 1 ? values[0] : "";
        String version = values.length >= 2 ? values[1] : "";

        Pageable pageable = PageRequest.of(0, query.getSize());
        return technoRepository.findAllByKeyNameLikeAndKeyVersionLike(name, version, pageable)
                .stream()
                .map(TechnoDocument::toTechnoView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
    public List<AbstractPropertyView> onGetTechnoPropertiesQuery(GetTechnoPropertiesQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        TechnoDocument technoDocument = technoRepository.findPropertiesByTechnoKey(keyDocument);
        return AbstractPropertyDocument.toViews(technoDocument.getProperties());
    }

    @QueryHandler
    @Override
    @Timed
    public List<Techno.Key> onGetTechnosWithPasswordWithinQuery(GetTechnosWithPasswordWithinQuery query) {
        List<KeyDocument> technoKeys = KeyDocument.fromModelKeys(query.getTechnoKeys());
        return technoRepository.findTechnosWithPasswordWithin(technoKeys).stream()
                .map(TechnoDocument::getDomainKey)
                .collect(Collectors.toList());

    }

    public List<TechnoDocument> getTechnoDocumentsFromDomainInstances(List<Techno> technos, TemplateContainer.Key moduleKey) {
        technos = Optional.ofNullable(technos).orElseGet(Collections::emptyList);
        List<TechnoDocument> technoDocs = technoRepository.findAllByKeyIn(technos.stream()
                .map(techno -> new KeyDocument(techno.getKey()))
                .collect(Collectors.toList()));
        // On vérifie en mode parano / programmation défensive qu'on récupère bien le bon nombre de documents.
        // Cette vérification permet par exemple de détecter lors d'un ModuleCreatedEvent si les technos requises sont absentes.
        if (technoDocs.size() != technos.size()) {
            Set<TemplateContainer.Key> technoKeys = technos.stream().map(TemplateContainer::getKey).collect(Collectors.toSet());
            Set<TemplateContainer.Key> retrievedTechnoKeys = technoDocs.stream().map(TechnoDocument::getDomainKey).collect(Collectors.toSet());
            technoKeys.removeAll(retrievedTechnoKeys);
            throw new NotFoundException("Techno not found among " + technos.size() + " requested by module " + moduleKey.getNamespaceWithoutPrefix() + ", no techno was found in repository for the following keys: " + technoKeys);
        }
        return technoDocs;
    }

    public List<TemplateContainerKeyView> getTechnoKeysForTechnoIds(List<String> technoIds) {
        return technoRepository.findKeysByIdsIn(technoIds)
                .stream()
                .map(TechnoDocument::getKey)
                .map(KeyDocument::toKeyView)
                .collect(Collectors.toList());
    }
}
