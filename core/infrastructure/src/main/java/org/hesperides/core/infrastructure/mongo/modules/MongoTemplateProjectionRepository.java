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
package org.hesperides.core.infrastructure.mongo.modules;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.modules.*;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.hesperides.core.infrastructure.mongo.templatecontainers.KeyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.TemplateDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoTemplateProjectionRepository implements TemplateProjectionRepository {

    private final MongoModuleRepository moduleRepository;

    @Autowired
    public MongoTemplateProjectionRepository(MongoModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    /*** EVENT HANDLERS ***/

    @Override
    @EventHandler
    public void onTemplateCreatedEvent(TemplateCreatedEvent event) {
        KeyDocument keyDocument = new KeyDocument(event.getModuleKey());
        ModuleDocument moduleDocument = moduleRepository.findByKey(keyDocument);
        TemplateDocument templateDocument = new TemplateDocument(event.getTemplate());
        moduleDocument.addTemplate(templateDocument);
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @Override
    @EventHandler
    public void onTemplateUpdatedEvent(TemplateUpdatedEvent event) {
        KeyDocument keyDocument = new KeyDocument(event.getModuleKey());
        ModuleDocument moduleDocument = moduleRepository.findByKey(keyDocument);
        TemplateDocument templateDocument = new TemplateDocument(event.getTemplate());
        moduleDocument.updateTemplate(templateDocument);
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @Override
    @EventHandler
    public void onTemplateDeletedEvent(TemplateDeletedEvent event) {
        KeyDocument keyDocument = new KeyDocument(event.getModuleKey());
        ModuleDocument moduleDocument = moduleRepository.findByKey(keyDocument);
        moduleDocument.removeTemplate(event.getTemplateName());
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    /*** QUERY HANDLERS ***/

    @Override
    @QueryHandler
    public Optional<TemplateView> onGetTemplateByNameQuery(GetTemplateByNameQuery query) {
        Optional<TemplateView> optionalTemplateView = Optional.empty();

        String templateName = query.getTemplateName();
        KeyDocument keyDocument = new KeyDocument(query.getModuleKey());
        Optional<ModuleDocument> optionalModuleDocument = moduleRepository.findOptionalByKeyAndTemplatesName(keyDocument, templateName);

        if (optionalModuleDocument.isPresent()) {
            TemplateDocument templateDocument = optionalModuleDocument.get().findOptionalTemplateByName(templateName).get();
            TemplateContainer.Key moduleKey = query.getModuleKey();
            optionalTemplateView = Optional.of(templateDocument.toTemplateView(moduleKey));
        }
        return optionalTemplateView;
    }

    @Override
    @QueryHandler
    public List<TemplateView> onGetModuleTemplatesQuery(GetModuleTemplatesQuery query) {
        List<TemplateView> templateViews = new ArrayList<>();

        KeyDocument keyDocument = new KeyDocument(query.getModuleKey());
        Optional<ModuleDocument> optionalModuleDocument = moduleRepository.findOptionalByKey(keyDocument);

        if (optionalModuleDocument.isPresent() && optionalModuleDocument.get().getTemplates() != null) {
            TemplateContainer.Key moduleKey = query.getModuleKey();
            templateViews = optionalModuleDocument.get().getTemplates().stream()
                    .map(templateDocument -> templateDocument.toTemplateView(moduleKey))
                    .collect(Collectors.toList());
        }
        return templateViews;
    }
}
