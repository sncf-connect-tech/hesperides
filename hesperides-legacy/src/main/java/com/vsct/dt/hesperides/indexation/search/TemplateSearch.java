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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 13/10/2014.
 */
public class TemplateSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateSearch.class);

    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();
    private final Mustache mustacheSearchByNamespaceLike = mustacheFactory.compile("search.template.hesnamespace.like.mustache");
    private final Mustache mustacheSearchByExactNamespace = mustacheFactory.compile("search.template.hesnamespace.mustache");

    private final ObjectReader elasticSearchTemplateReader;
    private final ElasticSearchClient elasticSearchClient;

    public TemplateSearch(final ElasticSearchClient elasticSearchClient) {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        JavaType type = objectMapper.getTypeFactory().constructParametricType(ElasticSearchResponse.class, TemplateSearchResponse.class);
        this.elasticSearchTemplateReader = objectMapper.reader(type);
        this.elasticSearchClient = elasticSearchClient;
    }

    public Set<TemplateSearchResponse> getTemplatesByNamespaceLike(final String[] wildcards) {
        return getTemplatesByNamespaceLike(wildcards, false);
    }

    public Set<TemplateSearchResponse> getTemplatesByNamespaceLike(final String[] wildcards, final boolean isCaseSensitive) {
        String url = String.format("/templates/_search?size=%1$s", 50);

        List<String> actualSearchCaseWildcards = Arrays.asList(wildcards).stream().map(wildcard -> isCaseSensitive ? wildcard : wildcard.toLowerCase()).collect(Collectors.toList());

        String body = TemplateContentGenerator.from(mustacheSearchByNamespaceLike)
                .put("wildcards", new DecoratedCollection<>(actualSearchCaseWildcards))
                .generate();

        ElasticSearchResponse<TemplateSearchResponse> esResponse = elasticSearchClient.withResponseReader(elasticSearchTemplateReader)
                .post(url, body);

        return esResponse.streamOfData().collect(Collectors.toSet());
    }

    public Set<TemplateSearchResponse> getTemplatesByExactNamespace(final String namespace) {
        String url = "/templates/_search";

        String body = TemplateContentGenerator.from(mustacheSearchByExactNamespace)
                .put("namespace", namespace)
                .generate();

        ElasticSearchResponse<TemplateSearchResponse> esResponse = elasticSearchClient.withResponseReader(elasticSearchTemplateReader)
                .post(url, body);

        //Filter to retain only exact namespace
        return esResponse.streamOfData().filter(template -> template.getNamespace().equals(namespace)).collect(Collectors.toSet());
    }

}
