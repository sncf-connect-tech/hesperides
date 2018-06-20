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
package org.hesperides.infrastructure.mongo.modules;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.queries.TemplateView;
import org.hesperides.infrastructure.mongo.templatecontainers.KeyDocument;
import org.hesperides.infrastructure.mongo.templatecontainers.TemplateDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.domain.framework.Profiles.FAKE_MONGO;
import static org.hesperides.domain.framework.Profiles.MONGO;

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
    @EventSourcingHandler
    public void on(TemplateCreatedEvent event) {
        ModuleDocument moduleDocument = moduleRepository.findByKey(KeyDocument.fromDomainInstance(event.getModuleKey()));
        TemplateDocument templateDocument = TemplateDocument.fromDomainInstance(event.getTemplate());
        moduleDocument.addTemplate(templateDocument);
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @Override
    @EventSourcingHandler
    public void on(TemplateUpdatedEvent event) {
        ModuleDocument moduleDocument = moduleRepository.findByKey(KeyDocument.fromDomainInstance(event.getModuleKey()));
        TemplateDocument templateDocument = TemplateDocument.fromDomainInstance(event.getTemplate());
        moduleDocument.updateTemplate(templateDocument);
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    @Override
    @EventSourcingHandler
    public void on(TemplateDeletedEvent event) {
        ModuleDocument moduleDocument = moduleRepository.findByKey(KeyDocument.fromDomainInstance(event.getModuleKey()));
        moduleDocument.removeTemplate(event.getTemplateName());
        moduleDocument.extractPropertiesAndSave(moduleRepository);
    }

    /*** QUERY HANDLERS ***/

    @Override
    @QueryHandler
    public Optional<TemplateView> query(GetTemplateByNameQuery query) {
        Optional<TemplateView> optionalTemplateView = Optional.empty();

        TemplateContainer.Key moduleKey = query.getModuleKey();
        String templateName = query.getTemplateName();

        Optional<ModuleDocument> optionalModuleDocument = moduleRepository.findOptionalByKeyAndTemplatesName(KeyDocument.fromDomainInstance(moduleKey), templateName);
        if (optionalModuleDocument.isPresent()) {
            TemplateDocument templateDocument = optionalModuleDocument.get().findOptionalTemplateByName(templateName).get();
            optionalTemplateView = Optional.of(templateDocument.toTemplateView(moduleKey));
        }
        return optionalTemplateView;
    }

    @Override
    @QueryHandler
    public List<TemplateView> query(GetModuleTemplatesQuery query) {
        List<TemplateView> templateViews = new ArrayList<>();

        TemplateContainer.Key moduleKey = query.getModuleKey();
        Optional<ModuleDocument> optionalModuleDocument = moduleRepository.findOptionalByKey(KeyDocument.fromDomainInstance(moduleKey));

        if (optionalModuleDocument.isPresent() && optionalModuleDocument.get().getTemplates() != null) {
            templateViews = optionalModuleDocument.get().getTemplates().stream()
                    .map(templateDocument -> templateDocument.toTemplateView(moduleKey))
                    .collect(Collectors.toList());
        }
        return templateViews;
    }
}
