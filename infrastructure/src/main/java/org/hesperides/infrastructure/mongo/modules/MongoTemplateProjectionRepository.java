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
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.hesperides.infrastructure.mongo.templatecontainer.KeyDocument;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
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
        TemplateContainer.Key key = event.getModuleKey();
        ModuleDocument module = moduleRepository.findByKey(KeyDocument.fromDomainInstance(key));
        if (module.getTemplates() == null) {
            module.setTemplates(new ArrayList<>());
        }
        TemplateDocument template = TemplateDocument.fromDomainInstance(event.getTemplate());
        module.getTemplates().add(template);
        moduleRepository.save(module);
    }

    @Override
    @EventSourcingHandler
    public void on(TemplateUpdatedEvent event) {
        TemplateContainer.Key key = event.getModuleKey();
        ModuleDocument module = moduleRepository.findByKey(KeyDocument.fromDomainInstance(key));
        for (int i = 0; i < module.getTemplates().size(); i++) {
            if (module.getTemplates().get(i).getName().equalsIgnoreCase(event.getTemplate().getName())) {
                module.getTemplates().set(i, TemplateDocument.fromDomainInstance(event.getTemplate()));
                break;
            }
        }
        moduleRepository.save(module);
    }

    @Override
    @EventSourcingHandler
    public void on(TemplateDeletedEvent event) {
        TemplateContainer.Key key = event.getModuleKey();
        ModuleDocument module = moduleRepository.findByKey(KeyDocument.fromDomainInstance(key));
        module.getTemplates().removeIf(template -> template.getName().equalsIgnoreCase(event.getTemplateName()));
        moduleRepository.save(module);
    }

    /*** QUERY HANDLERS ***/

    @Override
    @QueryHandler
    public Optional<TemplateView> query(GetTemplateByNameQuery query) {
        Optional<TemplateView> result = Optional.empty();
        TemplateContainer.Key key = query.getModuleKey();

        ModuleDocument moduleDocument = moduleRepository.findByKeyAndTemplatesName(KeyDocument.fromDomainInstance(key), query.getTemplateName());

        if (moduleDocument != null) {
            TemplateDocument templateDocument = moduleDocument.getTemplates().stream()
                    .filter(template -> template.getName().equalsIgnoreCase(query.getTemplateName()))
                    .findAny().get();
            result = Optional.of(templateDocument.toTemplateView(query.getModuleKey(), Module.KEY_PREFIX));
        }
        return result;
    }

    @Override
    @QueryHandler
    public List<TemplateView> query(GetModuleTemplatesQuery query) {
        List<TemplateView> result = new ArrayList<>();

        TemplateContainer.Key key = query.getModuleKey();
        ModuleDocument moduleDocument = moduleRepository.findByKey(KeyDocument.fromDomainInstance(key));

        if (moduleDocument != null && moduleDocument.getTemplates() != null) {
            result = moduleDocument.getTemplates().stream().map(templateDocument -> templateDocument.toTemplateView(key, Module.KEY_PREFIX)).collect(Collectors.toList());
        }

        return result;
    }
}
