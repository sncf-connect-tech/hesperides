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
import org.hesperides.domain.modules.GetModuleTemplatesQuery;
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
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        ModuleDocument moduleDocument = repository.findByNameAndVersionAndWorkingCopyAndTemplatesName(
                key.getName(), key.getVersion(), key.isWorkingCopy(), query.getTemplateName());

        if (moduleDocument != null) {
            TemplateDocument templateDocument = moduleDocument.getTemplates().stream()
                    .filter(template -> template.getName().equalsIgnoreCase(query.getTemplateName()))
                    .findAny().get();
            result = Optional.of(templateDocument.toTemplateView(query.getModuleKey(), Module.NAMESPACE_PREFIX));
        }
        return result;
    }

    @Override
    @QueryHandler
    public List<TemplateView> query(GetModuleTemplatesQuery query) {
        List<TemplateView> result = new ArrayList<>();

        TemplateContainer.Key key = query.getModuleKey();
        ModuleDocument moduleDocument = repository.findByNameAndVersionAndWorkingCopy(key.getName(), key.getVersion(), key.isWorkingCopy());

        if (moduleDocument != null) {
            result = moduleDocument.getTemplates().stream().map(templateDocument -> templateDocument.toTemplateView(key, Module.NAMESPACE_PREFIX)).collect(Collectors.toList());
        }

        return result;
    }
}
