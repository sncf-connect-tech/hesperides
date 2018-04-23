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
package org.hesperides.infrastructure.mongo.technos.queries;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.technos.GetTemplateQuery;
import org.hesperides.domain.technos.TechnoAlreadyExistsQuery;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.technos.queries.TechnoQueriesRepository;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.hesperides.infrastructure.mongo.technos.MongoTechnoRepository;
import org.hesperides.infrastructure.mongo.technos.TechnoDocument;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Profile("mongo")
@Component
public class MongoTechnoQueriesRepository implements TechnoQueriesRepository {

    private final MongoTechnoRepository repository;

    @Autowired
    public MongoTechnoQueriesRepository(MongoTechnoRepository repository) {
        this.repository = repository;
    }

    @QueryHandler
    public Optional<TemplateView> query(GetTemplateQuery query) {
        Optional<TemplateView> result = Optional.empty();

        /**
         * C'est moche mais je ne sais pas comment récupérer un template dans la collection de technos
         * à partir de la clé de la techno et du nom unique du template
         * TODO Améliorer
         */
        TechnoDocument technoDocumentSample = TechnoDocument.fromDomainKey(query.getTechnoKey());
        TechnoDocument technoDocument = repository.findOne(Example.of(technoDocumentSample));
        for (TemplateDocument templateDocument : technoDocument.getTemplates()) {
            if (templateDocument.getName().equalsIgnoreCase(query.getTemplateName())) {
                result = Optional.of(templateDocument.toTemplateView(query.getTechnoKey(), Techno.NAMESPACE_PREFIX));
                break;
            }
        }
        return result;
    }

    @QueryHandler
    public Boolean query(TechnoAlreadyExistsQuery query) {
        return repository.exists(Example.of(TechnoDocument.fromDomainKey(query.getTechnoKey())));
    }
}
