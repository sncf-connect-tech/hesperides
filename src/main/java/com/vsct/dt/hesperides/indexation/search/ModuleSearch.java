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
    private final Mustache mustacheSearchByNameAndVersionAndWorkingcopy = mustacheFactory.compile("search.module.name.version.workingcopy.mustache");
    private final Mustache mustacheSearchByNameAndVersionLike = mustacheFactory.compile("search.module.name.version.like.mustache");
    private final Mustache mustacheSearchAll = mustacheFactory.compile("search.module.all.mustache");
    private final Mustache mustacheSearchModuleByName = mustacheFactory.compile("search.module.name.mustache");

    private final ObjectReader elasticSearchModuleReader;
    private final ElasticSearchClient elasticSearchClient;

    public ModuleSearch(final ElasticSearchClient elasticSearchClient) {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        JavaType type = objectMapper.getTypeFactory().constructParametricType(ElasticSearchResponse.class, ModuleSearchResponse.class);
        this.elasticSearchModuleReader = objectMapper.reader(type);
        this.elasticSearchClient = elasticSearchClient;
    }

    /**
     * Return list of modules by name and version
     *
     * @param name
     * @param version
     *
     * @return list of module
     */
    public List<ModuleSearchResponse> getModulesByNameAndVersionLike(final String name, final String version) {
        String url = String.format("/modules/_search?size=%1$s", 50);

        String body = TemplateContentGenerator.from(mustacheSearchByNameAndVersionLike)
                .put("raw_name", name)
                .put("tokenized_name", name.replace(' ', '*').replace('-', '*'))
                .put("raw_version", version)
                .generate();

        ElasticSearchResponse<ModuleSearchResponse> esResponse = elasticSearchClient.withResponseReader(elasticSearchModuleReader)
                .post(url, body);

        return esResponse.streamOfData().collect(Collectors.toList());
    }

    /**
     * Get module by name and version.
     *
     * @param name module name
     * @param version version
     * @param workingcopy if is working copy
     *
     * @return list of module
     */
    public List<ModuleSearchResponse> getModulesByNameAndVersion(final String name, final String version, final String workingcopy) {
        String url = String.format("/modules/_search?size=%1$s", 1);

        String body = TemplateContentGenerator.from(mustacheSearchByNameAndVersionAndWorkingcopy)
                .put("name", name)
                .put("version", version)
                .put("is_working_copy", workingcopy)
                .generate();

        ElasticSearchResponse<ModuleSearchResponse> esResponse = elasticSearchClient.withResponseReader(elasticSearchModuleReader)
                .post(url, body);

        return esResponse.streamOfData().collect(Collectors.toList());
    }

    /**
     * Get module by name and version.
     *
     * @param name module name
     * @param version version
     *
     * @return list of module
     */
    public List<ModuleSearchResponse> getModulesByNameAndVersion(final String name, final String version) {
        String url = String.format("/modules/_search?size=%1$s", 1);

        String body = TemplateContentGenerator.from(mustacheSearchByNameAndVersion)
                .put("name", name.toLowerCase())
                .put("version", version)
                .generate();

        ElasticSearchResponse<ModuleSearchResponse> esResponse = elasticSearchClient.withResponseReader(elasticSearchModuleReader)
                .post(url, body);

        return esResponse.streamOfData().collect(Collectors.toList());
    }

    /**
     * Return list of module by name.
     *
     * @param name search name
     *
     * @return list of module
     */
    public List<ModuleSearchResponse> getModulesByName(final String name) {
        String url = "/modules/_search";

        String body = TemplateContentGenerator.from(mustacheSearchModuleByName)
                .put("name", name.toLowerCase())
                .generate();

        ElasticSearchResponse<ModuleSearchResponse> esResponse = elasticSearchClient.withResponseReader(elasticSearchModuleReader)
                .post(url, body);

        return esResponse.streamOfData().collect(Collectors.toList());
    }

    /**
     * Return list of modules.
     *
     * @return list of module
     */
    public List<ModuleSearchResponse> getAllModules() {
        String url = "/modules/_search";

        String body = TemplateContentGenerator.from(mustacheSearchAll)
                .generate();

        ElasticSearchResponse<ModuleSearchResponse> esResponse = elasticSearchClient.withResponseReader(elasticSearchModuleReader)
                .post(url, body);

        return esResponse.streamOfData().collect(Collectors.toList());
    }
}
