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
import com.vsct.dt.hesperides.indexation.ElasticSearchClient;
import com.vsct.dt.hesperides.indexation.model.ElasticSearchResponse;
import com.vsct.dt.hesperides.util.TemplateContentGenerator;
import io.dropwizard.jackson.Jackson;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 09/10/2014.
 */
public class ApplicationSearch {

    public static final int       SEARCH_SIZE     = 50;
    /**
     * Mustache factory.
     */
    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();
    /**
     * The Mustache Template for elastic search requests.
     */
    private final Mustache mustacheSearchByNameLike = mustacheFactory.compile("search.application.name.like.mustache");
    private final Mustache mustacheSearchByName = mustacheFactory.compile("search.application.name.mustache");
    private final Mustache mustacheSearchAllPlatform = mustacheFactory.compile("search.platform.name.like.mustache");
    private final Mustache mustacheSearchAllPlatformUsingModules = mustacheFactory.compile("search.platform.using.modules.mustache");
    /**
     * The elasticSearch Client Helper.
     */
    private final ElasticSearchClient elasticSearchClient;
    /**
     * A jackson reader for elastic search responses.
     */
    private final ObjectReader elasticSearchVsctApplicationReader;
    private final ObjectReader elasticSearchVsctPlatformReader;
    private final ObjectReader elasticSearchVsctPlatformApplicationReader;

    /**
     * Main constructor.
     *
     * @param elasticSearchClient
     */
    public ApplicationSearch(final ElasticSearchClient elasticSearchClient) {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        JavaType searchType = objectMapper.getTypeFactory().constructParametricType(ElasticSearchResponse.class, ApplicationSearchResponse.class);
        this.elasticSearchVsctApplicationReader = objectMapper.reader(searchType);
        JavaType searchTypePlatform = objectMapper.getTypeFactory().constructParametricType(ElasticSearchResponse.class, PlatformSearchResponse.class);
        this.elasticSearchVsctPlatformReader = objectMapper.reader(searchTypePlatform);
        JavaType searchTypePlatformApplication = objectMapper.getTypeFactory().constructParametricType(ElasticSearchResponse.class, PlatformApplicationSearchResponse.class);
        this.elasticSearchVsctPlatformApplicationReader = objectMapper.reader(searchTypePlatformApplication);
        this.elasticSearchClient = elasticSearchClient;
    }

    /**
     * Find an application with a name or just a part of a name.
     *
     * @param name
     * @return a set of applications matching request
     */
    public Set<ApplicationSearchResponse> getApplicationsLike(final String name) {
        String url = String.format("/platforms/_search?size=%1$s", SEARCH_SIZE);

        String body = TemplateContentGenerator.from(mustacheSearchByNameLike)
                .put("applicationName", name.toLowerCase())
                .generate();

        ElasticSearchResponse<ApplicationSearchResponse> esResponse = elasticSearchClient.withResponseReader(elasticSearchVsctApplicationReader)
                .post(url, body);

        return esResponse.streamOfData().collect(Collectors.toSet());
    }

    /**
     * Find an application with a name or just a part of a name.
     *
     * @param name
     * @return a set of applications matching request
     */
    public Set<ApplicationSearchResponse> getApplications(final String name) {
        String url = "/platforms/_search";

        String body = TemplateContentGenerator.from(mustacheSearchByName)
                .put("applicationName", name.toLowerCase())
                .generate();

        ElasticSearchResponse<ApplicationSearchResponse> esResponse = elasticSearchClient.withResponseReader(elasticSearchVsctApplicationReader)
                .post(url, body);

        return esResponse.streamOfData().collect(Collectors.toSet());
    }

    /**
     * Find all platforms for a given application name.
     *
     * @return a set of plaforms matching request
     */
    public Set<PlatformSearchResponse> getAllPlatforms(final String name, final String platformName) {
        String url = String.format("/platforms/_search?size=%1$s", SEARCH_SIZE);

        String body = TemplateContentGenerator.from(mustacheSearchAllPlatform)
                .put("applicationName", name.toLowerCase())
                .put("platformName", platformName)
                .generate();

        ElasticSearchResponse<PlatformSearchResponse> esResponse = elasticSearchClient.withResponseReader(elasticSearchVsctPlatformReader)
                .post(url, body);

        return esResponse.streamOfData().collect(Collectors.toSet());
    }

    /**
     * Find all platforms using by a module.
     *
     * @return a set of plaforms matching request
     */
    public Set<PlatformApplicationSearchResponse> getAllPlatformsUsingModules(final String moduleName, final String moduleVersion, final String isWorkingCopy) {
        String url = String.format("/platforms/_search?size=%1$s", SEARCH_SIZE);

        boolean boolIsWorkingCopy = true;
        if (isWorkingCopy.contains("release")) {
            boolIsWorkingCopy = false;
        }

        String body = TemplateContentGenerator.from(mustacheSearchAllPlatformUsingModules)
                .put("moduleName", moduleName)
                .put("moduleVersion", moduleVersion)
                .put("isWorkingCopy", boolIsWorkingCopy)
                .generate();

        ElasticSearchResponse<PlatformApplicationSearchResponse> esResponse = elasticSearchClient.withResponseReader(elasticSearchVsctPlatformApplicationReader)
                .post(url, body);

        return esResponse.streamOfData().collect(Collectors.toSet());
    }
}
