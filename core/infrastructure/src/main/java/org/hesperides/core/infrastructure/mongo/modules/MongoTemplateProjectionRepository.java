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

import io.micrometer.core.annotation.Timed;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.exceptions.NotFoundException;
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

import static org.hesperides.commons.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoTemplateProjectionRepository implements TemplateProjectionRepository {

    private MongoModuleRepository moduleRepository;

    @Autowired
    public MongoTemplateProjectionRepository(MongoModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    /*** EVENT HANDLERS ***/

    @EventHandler
    @Override
    @Timed
    public void onTemplateCreatedEvent(TemplateCreatedEvent event) {
        Optional<ModuleDocument> optModuleDocument = moduleRepository.findById(event.getModuleId());
        if (!optModuleDocument.isPresent()) {
            throw new NotFoundException("Module not found - template creation impossible - module: " + event.getModuleKey().getNamespaceWithoutPrefix());
        }
        ModuleDocument moduleDocument = optModuleDocument.get();
        TemplateDocument templateDocument = new TemplateDocument(event.getTemplate());
        moduleDocument.addTemplate(templateDocument);
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @EventHandler
    @Override
    @Timed
    public void onTemplateUpdatedEvent(TemplateUpdatedEvent event) {
        Optional<ModuleDocument> optModuleDocument = moduleRepository.findById(event.getModuleId());
        if (!optModuleDocument.isPresent()) {
            throw new NotFoundException("Module not found - template update impossible - module: " + event.getModuleKey().getNamespaceWithoutPrefix());
        }
        ModuleDocument moduleDocument = optModuleDocument.get();
        TemplateDocument templateDocument = new TemplateDocument(event.getTemplate());
        moduleDocument.updateTemplate(templateDocument);
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @EventHandler
    @Override
    @Timed
    public void onTemplateDeletedEvent(TemplateDeletedEvent event) {
        Optional<ModuleDocument> optModuleDocument = moduleRepository.findById(event.getModuleId());
        if (!optModuleDocument.isPresent()) {
            throw new NotFoundException("Module not found - template deletion impossible - module: " + event.getModuleKey().getNamespaceWithoutPrefix());
        }
        ModuleDocument moduleDocument = optModuleDocument.get();
        moduleDocument.removeTemplate(event.getTemplateName());
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    @Timed
    public Optional<TemplateView> onGetTemplateByNameQuery(GetTemplateByNameQuery query) {
        String templateName = query.getTemplateName();
        return moduleRepository.findByKeyAndTemplateName(new KeyDocument(query.getModuleKey()), templateName)
                .flatMap(moduleDocument -> Optional.ofNullable(moduleDocument.getTemplates())
                        .flatMap(templates -> templates.stream()
                                .filter(templateDocument -> templateDocument.getName().equalsIgnoreCase(templateName))
                                .findFirst()
                                .map(templateDocument -> templateDocument.toTemplateView(query.getModuleKey()))));
    }

    @QueryHandler
    @Override
    @Timed
    public List<TemplateView> onGetModuleTemplatesQuery(GetModuleTemplatesQuery query) {
        TemplateContainer.Key moduleKey = query.getModuleKey();
        return moduleRepository.findTemplatesByModuleKey(new KeyDocument(moduleKey))
                .map(moduleDocument -> moduleDocument.getTemplates()
                        .stream()
                        .map(templateDocument -> templateDocument.toTemplateView(moduleKey))
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }
}
