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
import org.hesperides.domain.technos.queries.TechnoView;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.domain.templatecontainers.queries.TemplateView;
import org.hesperides.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.infrastructure.mongo.templatecontainers.KeyDocument;
import org.hesperides.infrastructure.mongo.templatecontainers.TemplateDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.domain.framework.Profiles.FAKE_MONGO;
import static org.hesperides.domain.framework.Profiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
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
        TechnoDocument technoDocument = TechnoDocument.fromDomainInstance(event.getTechno());
        technoDocument.extractPropertiesAndSave(technoRepository);
    }

    @Override
    public void on(TechnoDeletedEvent event) {
        technoRepository.deleteByKey(KeyDocument.fromDomainInstance(event.getTechnoKey()));
    }

    @EventSourcingHandler
    @Override
    public void on(TemplateAddedToTechnoEvent event) {
        TemplateContainer.Key key = event.getTechnoKey();
        TechnoDocument technoDocument = technoRepository.findByKey(KeyDocument.fromDomainInstance(key));
        TemplateDocument templateDocument = TemplateDocument.fromDomainInstance(event.getTemplate());
        technoDocument.addTemplate(templateDocument);
        technoDocument.extractPropertiesAndSave(technoRepository);
    }

    @Override
    public void on(TechnoTemplateUpdatedEvent event) {
        TechnoDocument technoDocument = technoRepository.findByKey(KeyDocument.fromDomainInstance(event.getTechnoKey()));
        TemplateDocument templateDocument = TemplateDocument.fromDomainInstance(event.getTemplate());
        technoDocument.updateTemplate(templateDocument);
        technoDocument.extractPropertiesAndSave(technoRepository);
    }

    @Override
    public void on(TechnoTemplateDeletedEvent event) {
        TechnoDocument technoDocument = technoRepository.findByKey(KeyDocument.fromDomainInstance(event.getTechnoKey()));
        technoDocument.removeTemplate(event.getTemplateName());
        technoDocument.extractPropertiesAndSave(technoRepository);
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
            optionalTemplateView = Optional.of(templateDocument.toTemplateView(key));
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

    @Override
    public List<TemplateView> query(GetTemplatesQuery query) {
        List<TemplateView> templateViews = new ArrayList<>();
        TemplateContainer.Key key = query.getTechnoKey();

        Optional<TechnoDocument> optionalTechnoDocument = technoRepository.findOptionalByKey(KeyDocument.fromDomainInstance(key));

        if (optionalTechnoDocument.isPresent()) {
            templateViews = optionalTechnoDocument.get().getTemplates().stream()
                    .map(templateDocument -> templateDocument.toTemplateView(key))
                    .collect(Collectors.toList());
        }
        return templateViews;
    }

    @Override
    public Optional<TechnoView> query(GetTechnoQuery query) {
        Optional<TechnoView> optionalTechnoView = Optional.empty();
        Optional<TechnoDocument> optionalTechnoDocument = technoRepository.findOptionalByKey(KeyDocument.fromDomainInstance(query.getTechnoKey()));
        if (optionalTechnoDocument.isPresent()) {
            optionalTechnoView = Optional.of(optionalTechnoDocument.get().toTechnoView());
        }
        return optionalTechnoView;
    }

    @Override
    public List<TechnoView> query(SearchTechnosQuery query) {
        String[] values = query.getInput().split(" ");
        String name = values.length >= 1 ? values[0] : "";
        String version = values.length >= 2 ? values[1] : "";

        Pageable pageableRequest = new PageRequest(0, 10); //TODO Sortir cette valeur dans le fichier de configuration
        List<TechnoDocument> technoDocuments = technoRepository.findAllByKeyNameLikeAndAndKeyVersionLike(name, version, pageableRequest);
        return technoDocuments.stream().map(TechnoDocument::toTechnoView).collect(Collectors.toList());
    }

    @Override
    public List<AbstractPropertyView> query(GetTechnoPropertiesQuery query) {
        TechnoDocument technoDocument = technoRepository.findByKey(KeyDocument.fromDomainInstance(query.getTechnoKey()));
        return AbstractPropertyDocument.toAbstractPropertyViews(technoDocument.getProperties());
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
