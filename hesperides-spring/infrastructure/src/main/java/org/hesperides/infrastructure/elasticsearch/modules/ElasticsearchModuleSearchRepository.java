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
package org.hesperides.infrastructure.elasticsearch.modules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.queries.ModulesNamesQuery;
import org.hesperides.infrastructure.elasticsearch.ElasticsearchService;
import org.hesperides.infrastructure.elasticsearch.response.Hit;
import org.hesperides.infrastructure.elasticsearch.response.ResponseHits;
import org.hesperides.infrastructure.mustache.MustacheTemplateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Profile("!local")
public class ElasticsearchModuleSearchRepository {

    @Autowired
    ElasticsearchService elasticsearchService;
    MustacheFactory mustacheFactory = new DefaultMustacheFactory();
    private static final String MUSTACHE_SEARCH_ALL = "search.module.all.mustache";

    @QueryHandler
    private List<String> queryAllModuleNames(ModulesNamesQuery query) {
        Mustache mustache = mustacheFactory.compile(MUSTACHE_SEARCH_ALL);
        String requestBody = MustacheTemplateGenerator.from(mustache).generate();
        ResponseHits responseHits = elasticsearchService.getResponseHits("POST", "/modules/_search", requestBody, new TypeReference<ResponseHits<ElasticsearchModule>>() {
        });

        return elasticSearchModulesToModuleNames(responseHits);
    }

    private List<String> elasticSearchModulesToModuleNames(final ResponseHits responseHits) {
        List<String> modules = new ArrayList<>();
        if (responseHits != null && responseHits.getHits() != null && responseHits.getHits().getHits() != null) {
            List<Hit<ElasticsearchModule>> hits = responseHits.getHits().getHits();
            for (Hit<ElasticsearchModule> hit : hits) {
                ElasticsearchModule elasticSearchModule = hit.getSource();
                modules.add(elasticSearchModule.getName());
            }
        }
        return modules;
    }
}
