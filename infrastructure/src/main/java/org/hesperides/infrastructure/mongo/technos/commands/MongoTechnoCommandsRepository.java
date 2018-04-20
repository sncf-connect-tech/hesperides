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
package org.hesperides.infrastructure.mongo.technos.commands;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.hesperides.domain.technos.TechnoCreatedEvent;
import org.hesperides.domain.technos.TemplateAddedToTechnoEvent;
import org.hesperides.domain.technos.commands.TechnoCommandsRepository;
import org.hesperides.infrastructure.mongo.technos.MongoTechnoRepository;
import org.hesperides.infrastructure.mongo.technos.TechnoDocument;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Profile("mongo")
@Repository
public class MongoTechnoCommandsRepository implements TechnoCommandsRepository {
    private final MongoTechnoRepository repository;

    @Autowired
    public MongoTechnoCommandsRepository(MongoTechnoRepository repository) {
        this.repository = repository;
    }

    @EventSourcingHandler
    @Override
    public void on(TechnoCreatedEvent event) {
        repository.save(TechnoDocument.fromDomain(event.getTechno()));
    }

    @EventSourcingHandler
    @Override
    public void on(TemplateAddedToTechnoEvent event) {
        TechnoDocument technoWithKeyOnly = TechnoDocument.fromDomainKey(event.getTechnoKey());
        TechnoDocument actualTechno = repository.findOne(Example.of(technoWithKeyOnly));
        TemplateDocument newTemplate = TemplateDocument.fromDomain(event.getTemplate());
        if (actualTechno.getTemplates() == null) {
            actualTechno.setTemplates(new ArrayList<>());
        }
        actualTechno.getTemplates().add(newTemplate);
        repository.save(actualTechno);
    }
}
