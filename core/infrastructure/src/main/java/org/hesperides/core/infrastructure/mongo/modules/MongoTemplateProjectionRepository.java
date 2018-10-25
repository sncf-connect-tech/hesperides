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

import java.util.Collections;
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

    @EventHandler
    @Override
    public void onTemplateCreatedEvent(TemplateCreatedEvent event) {
        ModuleDocument moduleDocument = moduleRepository.findOne(event.getId());
        TemplateDocument templateDocument = new TemplateDocument(event.getTemplate());
        moduleDocument.addTemplate(templateDocument);
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @EventHandler
    @Override
    public void onTemplateUpdatedEvent(TemplateUpdatedEvent event) {
        ModuleDocument moduleDocument = moduleRepository.findOne(event.getId());
        TemplateDocument templateDocument = new TemplateDocument(event.getTemplate());
        moduleDocument.updateTemplate(templateDocument);
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @EventHandler
    @Override
    public void onTemplateDeletedEvent(TemplateDeletedEvent event) {
        ModuleDocument moduleDocument = moduleRepository.findOne(event.getId());
        moduleDocument.removeTemplate(event.getTemplateName());
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    public Optional<TemplateView> onGetTemplateByNameQuery(GetTemplateByNameQuery query) {
        String templateName = query.getTemplateName();
        return moduleRepository.findOptionalByKeyAndTemplatesName(new KeyDocument(query.getModuleKey()), templateName)
                .map(moduleDocument -> moduleDocument.getTemplates()
                        .stream()
                        .filter(templateDocument -> templateDocument.getName().equalsIgnoreCase(templateName))
                        .findFirst()
                        .map(templateDocument -> templateDocument.toTemplateView(query.getModuleKey())))
                .orElse(Optional.empty());
    }

    @QueryHandler
    @Override
    public List<TemplateView> onGetModuleTemplatesQuery(GetModuleTemplatesQuery query) {
        TemplateContainer.Key moduleKey = query.getModuleKey();
        return moduleRepository.findOptionalByKey(new KeyDocument(moduleKey))
                .map(moduleDocument -> moduleDocument.getTemplates()
                        .stream()
                        .map(templateDocument -> templateDocument.toTemplateView(moduleKey))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
