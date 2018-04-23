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
package org.hesperides.infrastructure.mongo.modules.queries;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.GetTemplateByNameQuery;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.TemplateQueriesRepository;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.hesperides.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.infrastructure.mongo.modules.MongoModuleRepository;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public class MongoTemplateQueriesRepository implements TemplateQueriesRepository {

    private final MongoModuleRepository repository;

    @Autowired
    public MongoTemplateQueriesRepository(MongoModuleRepository repository) {
        this.repository = repository;
    }

    @Override
    @QueryHandler
    public Optional<TemplateView> query(GetTemplateByNameQuery query) {
        Optional<TemplateView> result = Optional.empty();

        TemplateContainer.Key key = query.getModuleKey();
        ModuleDocument searchDocument = new ModuleDocument();
        searchDocument.setName(key.getName());
        searchDocument.setVersion(key.getVersion());
        searchDocument.setVersionType(key.getVersionType());

        ModuleDocument module = repository.findOne(Example.of(searchDocument));
        for (TemplateDocument templateDocument : module.getTemplates()) {
            if (templateDocument.getName().equalsIgnoreCase(query.getTemplateName())) {
                result = Optional.of(templateDocument.toTemplateView(query.getModuleKey(), Module.NAMESPACE_PREFIX));
            }
        }
        return result;
    }
}
