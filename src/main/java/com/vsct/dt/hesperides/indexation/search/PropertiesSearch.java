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
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 13/10/2014.
 */
public final class PropertiesSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesSearch.class);
    public static final int SEARCH_SIZE = 50;
    private final MustacheFactory mustacheFactory               = new DefaultMustacheFactory();
    private final Mustache        mustacheSearchByNamespaceLike = mustacheFactory.compile("search.properties.hesnamespace.like.mustache");

    private final ObjectReader        elasticSearchPropertiesReader;
    private final ElasticSearchClient elasticSearchClient;

    public PropertiesSearch(final ElasticSearchClient elasticSearchClient) {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        JavaType type = objectMapper.getTypeFactory().constructParametricType(ElasticSearchResponse.class, PropertiesSearchResponse.class);
        this.elasticSearchPropertiesReader = objectMapper.reader(type);
        this.elasticSearchClient = elasticSearchClient;
    }

    public List<PropertiesSearchResponse> searchPropertiesByNamespaceLike(final String[] wildcards) {
        String url = String.format("/evaluatedproperties/_search?size=%1$s", SEARCH_SIZE);

        String body = TemplateContentGenerator.from(mustacheSearchByNamespaceLike)
                .put("wildcards", new DecoratedCollection<>(Arrays.asList(wildcards)))
                .generate();

        ElasticSearchResponse<PropertiesSearchResponse> esResponse = elasticSearchClient.withResponseReader(elasticSearchPropertiesReader)
                .post(url, body);
        LOGGER.debug("search properties: {} hits", esResponse.getHitsNumber());
        return esResponse.streamOfData().collect(Collectors.toList());
    }

}
