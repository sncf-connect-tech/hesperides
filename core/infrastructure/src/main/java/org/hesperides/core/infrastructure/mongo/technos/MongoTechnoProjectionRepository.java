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

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.technos.*;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.hesperides.core.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.core.infrastructure.mongo.modules.MongoModuleRepository;
import org.hesperides.core.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.KeyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.TemplateDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoTechnoProjectionRepository implements TechnoProjectionRepository {

    private final MongoTechnoRepository technoRepository;
    private final MongoModuleRepository moduleRepository;

    @Autowired
    public MongoTechnoProjectionRepository(MongoTechnoRepository technoRepository, MongoModuleRepository moduleRepository) {
        this.technoRepository = technoRepository;
        this.moduleRepository = moduleRepository;
    }

    /*** EVENT HANDLERS ***/

    @EventHandler
    @Override
    public void onTechnoCreatedEvent(TechnoCreatedEvent event) {
        TechnoDocument technoDocument = new TechnoDocument(event.getId(), event.getTechno());
        technoDocument.extractPropertiesAndSave(technoRepository);
    }

    @Override
    public void onTechnoDeletedEvent(TechnoDeletedEvent event) {
        removeReferencesAndUpdateProperties(event.getId());
        technoRepository.delete(event.getId());
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
        TechnoDocument technoDocument = technoRepository.findOne(event.getId());
        TemplateDocument templateDocument = new TemplateDocument(event.getTemplate());
        technoDocument.addTemplate(templateDocument);
        technoDocument.extractPropertiesAndSave(technoRepository);
        updateModelUsingTechno(event.getId());
    }

    @EventHandler
    @Override
    public void onTechnoTemplateUpdatedEvent(TechnoTemplateUpdatedEvent event) {
        TechnoDocument technoDocument = technoRepository.findOne(event.getId());
        TemplateDocument templateDocument = new TemplateDocument(event.getTemplate());
        technoDocument.updateTemplate(templateDocument);
        technoDocument.extractPropertiesAndSave(technoRepository);
        updateModelUsingTechno(event.getId());
    }

    @EventHandler
    @Override
    public void onTechnoTemplateDeletedEvent(TechnoTemplateDeletedEvent event) {
        TechnoDocument technoDocument = technoRepository.findOne(event.getId());
        technoDocument.removeTemplate(event.getTemplateName());
        technoDocument.extractPropertiesAndSave(technoRepository);
        updateModelUsingTechno(event.getId());
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
    public Boolean onTechnoAlreadyExistsQuery(TechnoAlreadyExistsQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        return technoRepository.countByKey(keyDocument) > 0;
    }

    @QueryHandler
    @Override
    public Optional<TemplateView> onGetTemplateQuery(GetTemplateQuery query) {
        TemplateContainer.Key technoKey = query.getTechnoKey();
        return technoRepository.findOptionalByKeyAndTemplatesName(new KeyDocument(technoKey), query.getTemplateName())
                .map(technoDocument -> technoDocument.getTemplates()
                        .stream()
                        .filter(templateDocument -> templateDocument.getName().equalsIgnoreCase(query.getTemplateName()))
                        .findFirst()
                        .map(templateDocument -> templateDocument.toTemplateView(technoKey)))
                .orElse(Optional.empty());
    }

    @QueryHandler
    @Override
    public List<TemplateView> onGetTemplatesQuery(GetTemplatesQuery query) {
        TemplateContainer.Key technoKey = query.getTechnoKey();
        return technoRepository.findOptionalByKey(new KeyDocument(technoKey))
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
        return technoRepository.findOptionalByKey(keyDocument)
                .map(TechnoDocument::toTechnoView);
    }

    @QueryHandler
    @Override
    public List<TechnoView> onSearchTechnosQuery(SearchTechnosQuery query) {
        String[] values = query.getInput().split(" ");
        String name = values.length >= 1 ? values[0] : "";
        String version = values.length >= 2 ? values[1] : "";

        Pageable pageable = new PageRequest(0, 10); //TODO Sortir cette valeur dans le fichier de configuration
        return technoRepository.findAllByKeyNameLikeAndKeyVersionLike(name, version, pageable)
                .stream()
                .map(TechnoDocument::toTechnoView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<AbstractPropertyView> onGetTechnoPropertiesQuery(GetTechnoPropertiesQuery query) {
        KeyDocument keyDocument = new KeyDocument(query.getTechnoKey());
        TechnoDocument technoDocument = technoRepository.findByKey(keyDocument);
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
