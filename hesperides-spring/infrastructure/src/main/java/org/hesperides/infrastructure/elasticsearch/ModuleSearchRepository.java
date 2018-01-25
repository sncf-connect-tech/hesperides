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
package org.hesperides.infrastructure.elasticsearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.hesperides.infrastructure.elasticsearch.mustache.MustacheTemplateGenerator;
import org.hesperides.infrastructure.elasticsearch.response.Hit;
import org.hesperides.infrastructure.elasticsearch.response.ResponseHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ModuleSearchRepository implements org.hesperides.domain.ModuleSearchRepository {

    @Autowired
    ElasticSearchService elasticSearchService;
    MustacheFactory mustacheFactory = new DefaultMustacheFactory();
    private static final String MUSTACHE_SEARCH_ALL = "search.module.all.mustache";

    @Override
    public List<org.hesperides.domain.Module> getModules() {
        Mustache mustache = mustacheFactory.compile(MUSTACHE_SEARCH_ALL);
        String requestBody = MustacheTemplateGenerator.from(mustache).generate();
        ResponseHits responseHits =elasticSearchService.getResponseHits("POST","/modules/_search",requestBody,new TypeReference<ResponseHits<Module>>(){
        });

        return elasticSearchModulesToDomainModules(responseHits);
    }

    private List<org.hesperides.domain.Module> elasticSearchModulesToDomainModules(final ResponseHits responseHits) {
        List<org.hesperides.domain.Module> modules = new ArrayList<>();
        if (responseHits != null && responseHits.getHits() != null && responseHits.getHits().getHits() != null) {
            List<Hit<Module>> hits = responseHits.getHits().getHits();
            for (Hit<Module> hit : hits) {
                Module elasticSearchModule = hit.getSource();
                org.hesperides.domain.Module module = elasticSearchModule.toDomainModule();
                modules.add(module);
            }
        }
        return modules;
    }
}
