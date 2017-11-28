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
package com.vsct.dt.hesperides.infrastructure.elasticsearch.modules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.vsct.dt.hesperides.domain.modules.Module;
import com.vsct.dt.hesperides.domain.modules.ModuleSearchRepository;
import com.vsct.dt.hesperides.infrastructure.elasticsearch.ElasticSearchService;
import com.vsct.dt.hesperides.infrastructure.elasticsearch.response.Hit;
import com.vsct.dt.hesperides.infrastructure.elasticsearch.response.ResponseHits;
import com.vsct.dt.hesperides.infrastructure.mustache.MustacheTemplateGenerator;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ElasticSearchModuleSearchRepository implements ModuleSearchRepository {

    private static final String MUSTACHE_SEARCH_ALL = "search.module.all.mustache";

    private final ElasticSearchService elasticSearchService;
    private final MustacheFactory mustacheFactory;

    @Inject
    public ElasticSearchModuleSearchRepository(final ElasticSearchService elasticSearchService, final MustacheFactory mustacheFactory) {
        this.elasticSearchService = elasticSearchService;
        this.mustacheFactory = mustacheFactory;
    }

    @Override
    public List<Module> getModules() {
        // Récupère le template Mustache contenant la requête ElasticSearch
        Mustache mustache = mustacheFactory.compile(MUSTACHE_SEARCH_ALL);
        String requestBody = MustacheTemplateGenerator.from(mustache).generate();
        // Récupère les résultats de cette requête
        ResponseHits responseHits = elasticSearchService.getResponseHits("POST", "/modules/_search", requestBody, new TypeReference<ResponseHits<ElasticSearchModule>>() {
        });
        // Mappe les résultats en objets du domaine
        return elasticSearchModulesToDomainModules(responseHits);
    }

    private List<Module> elasticSearchModulesToDomainModules(final ResponseHits responseHits) {
        List<Module> modules = new ArrayList<>();
        if (responseHits != null && responseHits.getHits() != null && responseHits.getHits().getHits() != null) {
            List<Hit<ElasticSearchModule>> hits = responseHits.getHits().getHits();
            for (Hit<ElasticSearchModule> hit : hits) {
                ElasticSearchModule elasticSearchModule = hit.getSource();
                Module module = elasticSearchModule.toDomainModule();
                modules.add(module);
            }
        }
        return modules;
    }
}
