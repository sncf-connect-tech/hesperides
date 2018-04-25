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
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.hesperides.infrastructure.mongo.technos.MongoTechnoRepository;
import org.hesperides.infrastructure.mongo.technos.TechnoDocument;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public class MongoTechnoQueriesRepository implements TechnoQueriesRepository {

    private final MongoTechnoRepository repository;

    @Autowired
    public MongoTechnoQueriesRepository(MongoTechnoRepository repository) {
        this.repository = repository;
    }

    @QueryHandler
    public Optional<TemplateView> query(GetTemplateQuery query) {
        Optional<TemplateView> result = Optional.empty();
        TemplateContainer.Key key = query.getTechnoKey();

        TechnoDocument technoDocument = repository.findByNameAndVersionAndWorkingCopyAndTemplatesName(
                key.getName(), key.getVersion(), key.isWorkingCopy(), query.getTemplateName());

        if (technoDocument != null) {
            TemplateDocument templateDocument = technoDocument.getTemplates().stream()
                    .filter(template -> template.getName().equalsIgnoreCase(query.getTemplateName()))
                    .findAny().get();
            result = Optional.of(templateDocument.toTemplateView(query.getTechnoKey(), Techno.NAMESPACE_PREFIX));
        }
        return result;
    }

    @QueryHandler
    public Boolean query(TechnoAlreadyExistsQuery query) {
        TemplateContainer.Key key = query.getTechnoKey();
        Optional<TechnoDocument> technoDocument = repository.findOptionalByNameAndVersionAndWorkingCopy(
                key.getName(), key.getVersion(), key.isWorkingCopy());
        return technoDocument.isPresent();
    }
}
