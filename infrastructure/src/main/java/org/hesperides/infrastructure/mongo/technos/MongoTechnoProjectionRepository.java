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
package org.hesperides.infrastructure.mongo.technos;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.technos.*;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.hesperides.infrastructure.mongo.templatecontainer.KeyDocument;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public class MongoTechnoProjectionRepository implements TechnoProjectionRepository {

    private final MongoTechnoRepository technoRepository;

    @Autowired
    public MongoTechnoProjectionRepository(MongoTechnoRepository technoRepository) {
        this.technoRepository = technoRepository;
    }

    @EventSourcingHandler
    @Override
    public void on(TechnoCreatedEvent event) {
        technoRepository.save(TechnoDocument.fromDomainInstance(event.getTechno()));
    }

    @EventSourcingHandler
    @Override
    public void on(TemplateAddedToTechnoEvent event) {
        TemplateContainer.Key key = event.getTechnoKey();
        TechnoDocument technoDocument = technoRepository.findByKey(KeyDocument.fromDomainInstance(key));
        TemplateDocument templateDocument = TemplateDocument.fromDomainInstance(event.getTemplate());
        technoDocument.addTemplate(templateDocument);
        technoRepository.save(technoDocument);
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    public Optional<TemplateView> query(GetTemplateQuery query) {
        Optional<TemplateView> optionalTemplateView = Optional.empty();
        TemplateContainer.Key key = query.getTechnoKey();

        Optional<TechnoDocument> optionalTechnoDocument = technoRepository.findOptionalByKeyAndTemplatesName(KeyDocument.fromDomainInstance(key), query.getTemplateName());

        if (optionalTechnoDocument.isPresent()) {
            TemplateDocument templateDocument = optionalTechnoDocument.get().getTemplates().stream()
                    .filter(template -> template.getName().equalsIgnoreCase(query.getTemplateName()))
                    .findAny().get();
            optionalTemplateView = Optional.of(templateDocument.toTemplateView(query.getTechnoKey(), Techno.KEY_PREFIX));
        }
        return optionalTemplateView;
    }

    @QueryHandler
    @Override
    public Boolean query(TechnoAlreadyExistsQuery query) {
        TemplateContainer.Key key = query.getTechnoKey();
        Optional<TechnoDocument> technoDocument = technoRepository.findOptionalByKey(KeyDocument.fromDomainInstance(key));
        return technoDocument.isPresent();
    }

    public List<TechnoDocument> getTechnoDocumentsFromDomainInstances(List<Techno> technos) {
        List<TechnoDocument> technoDocuments = null;
        if (technos != null) {
            List<KeyDocument> keyDocuments = technos.stream().map(techno -> KeyDocument.fromDomainInstance(techno.getKey())).collect(Collectors.toList());
            technoDocuments = technoRepository.findAllByKeyIn(keyDocuments);
        }
        return technoDocuments;
    }
}
