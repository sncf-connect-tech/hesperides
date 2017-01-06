/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides.indexation.search;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.util.DecoratedCollection;
import com.vsct.dt.hesperides.indexation.ElasticSearchClient;
import com.vsct.dt.hesperides.indexation.model.ElasticSearchResponse;
import com.vsct.dt.hesperides.util.TemplateContentGenerator;
import io.dropwizard.jackson.Jackson;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 09/12/2014.
 */
public class ModuleSearch {

    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();
    private final Mustache mustacheSearchByNameAndVersion = mustacheFactory.compile("search.module.name.version.mustache");
    private final Mustache mustacheSearchByNameAndVersionLike = mustacheFactory.compile("search.module.name.version.like.mustache");

    private final ObjectReader elasticSearchModuleReader;
    private final ElasticSearchClient elasticSearchClient;

    public ModuleSearch(final ElasticSearchClient elasticSearchClient) {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        JavaType type = objectMapper.getTypeFactory().constructParametricType(ElasticSearchResponse.class, ModuleSearchResponse.class);
        this.elasticSearchModuleReader = objectMapper.reader(type);
        this.elasticSearchClient = elasticSearchClient;
    }

    public List<ModuleSearchResponse> getModulesByNameAndVersionLike(final String[] wildcards) {
        String url = String.format("/modules/_search?size=%1$s", 50);

        List<String> insensitiveCaseWildcards = Arrays.asList(wildcards).stream().map(wildcard -> wildcard.toLowerCase()).collect(Collectors.toList());

//        String body = TemplateContentGenerator.from(mustacheSearchByNameAndVersionLike)
//                .put("wildcards", new DecoratedCollection<>(insensitiveCaseWildcards))
//                .generate();
//
        String body = TemplateContentGenerator.from(mustacheSearchByNameAndVersionLike)
                .put("raw_name", wildcards[0])
                .put("tokenized_name", wildcards[0].replace(' ', '*').replace('-', '*'))
                .put("raw_version", wildcards[1])
                .generate();

        ElasticSearchResponse<ModuleSearchResponse> esResponse = elasticSearchClient.withResponseReader(elasticSearchModuleReader)
                .post(url, body);

        return esResponse.streamOfData().collect(Collectors.toList());
    }

    public List<ModuleSearchResponse> getModulesByNameAndVersion(final String[] terms) {
        String url = String.format("/modules/_search?size=%1$s", 1);

        String body = TemplateContentGenerator.from(mustacheSearchByNameAndVersion)
                .put("name", terms[0])
                .put("version", terms[1])
                .put("is_working_copy", terms[2])
                .generate();

        ElasticSearchResponse<ModuleSearchResponse> esResponse = elasticSearchClient.withResponseReader(elasticSearchModuleReader)
                .post(url, body);

        return esResponse.streamOfData().collect(Collectors.toList());
    }
}
