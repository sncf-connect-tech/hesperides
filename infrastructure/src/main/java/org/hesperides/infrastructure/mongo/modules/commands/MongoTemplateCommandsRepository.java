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
package org.hesperides.infrastructure.mongo.modules.commands;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.hesperides.domain.modules.TemplateCreatedEvent;
import org.hesperides.domain.modules.TemplateDeletedEvent;
import org.hesperides.domain.modules.TemplateUpdatedEvent;
import org.hesperides.domain.modules.commands.TemplateCommandsRepository;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.infrastructure.mongo.modules.MongoModuleRepository;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public class MongoTemplateCommandsRepository implements TemplateCommandsRepository {

    private final MongoModuleRepository repository;

    @Autowired
    public MongoTemplateCommandsRepository(MongoModuleRepository repository) {
        this.repository = repository;
    }

    @Override
    @EventSourcingHandler
    public void on(TemplateCreatedEvent event) {
        //TODO Utiliser findByKey et trouver un moyen d'améliorer tout ça
        TemplateContainer.Key key = event.getModuleKey();
        ModuleDocument searchDocument = new ModuleDocument();
        searchDocument.setName(key.getName());
        searchDocument.setVersion(key.getVersion());
        searchDocument.setVersionType(key.getVersionType());

        ModuleDocument module = repository.findOne(Example.of(searchDocument));
        if (module.getTemplates() == null) {
            module.setTemplates(new ArrayList<>());
        }
        TemplateDocument template = TemplateDocument.fromDomain(event.getTemplate());
        module.getTemplates().add(template);
        repository.save(module);
    }

    @Override
    @EventSourcingHandler
    public void on(TemplateUpdatedEvent event) {
        //TODO Utiliser findByKey et trouver un moyen d'améliorer tout ça
        TemplateContainer.Key key = event.getModuleKey();
        ModuleDocument searchDocument = new ModuleDocument();
        searchDocument.setName(key.getName());
        searchDocument.setVersion(key.getVersion());
        searchDocument.setVersionType(key.getVersionType());

        ModuleDocument module = repository.findOne(Example.of(searchDocument));
        for (int i = 0; i < module.getTemplates().size(); i++) {
            if (module.getTemplates().get(i).getName().equalsIgnoreCase(event.getTemplate().getName())) {
                module.getTemplates().set(i, TemplateDocument.fromDomain(event.getTemplate()));
            }
        }
        repository.save(module);
    }

    @Override
    @EventSourcingHandler
    public void on(TemplateDeletedEvent event) {
        TemplateContainer.Key key = event.getModuleKey();
        ModuleDocument searchDocument = new ModuleDocument();
        searchDocument.setName(key.getName());
        searchDocument.setVersion(key.getVersion());
        searchDocument.setVersionType(key.getVersionType());

        ModuleDocument module = repository.findOne(Example.of(searchDocument));
        for (int i = 0; i < module.getTemplates().size(); i++) {
            if (module.getTemplates().get(i).getName().equalsIgnoreCase(event.getTemplateName())) {
                module.getTemplates().remove(i);
            }
        }
        repository.save(module);
    }
}
