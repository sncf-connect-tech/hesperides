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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsct.dt.hesperides.indexation.ElasticSearchClient;
import com.vsct.dt.hesperides.indexation.model.ElasticSearchResponse;
import io.dropwizard.jackson.Jackson;
import tests.type.UnitTests;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.Set;

import static com.vsct.dt.hesperides.ElasticSearchMock.elasticSearchResponseWithEntities;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by william_montaz on 13/10/2014.
 */
@Category(UnitTests.class)
public class TemplateSearchTest {

    final private ElasticSearchClient elasticSearchClient = mock(ElasticSearchClient.class);
    final private ElasticSearchClient.RequestExecuter executer = mock(ElasticSearchClient.RequestExecuter.class);
    private TemplateSearch templateSearch;

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Before
    public void resetMock() {
        reset(elasticSearchClient);
        reset(executer);
        when(elasticSearchClient.withResponseReader(any())).thenReturn(executer);
        templateSearch = new TemplateSearch(elasticSearchClient);
    }

    @Test
    public void testGetTemplatesByNamespaceLike() throws IOException {
        TemplateSearchResponse template1 = new TemplateSearchResponse("some_namespace", "t1");
        TemplateSearchResponse template2 = new TemplateSearchResponse("some_namespace", "t2");
        ElasticSearchResponse<TemplateSearchResponse> elasticSearchResponse = elasticSearchResponseWithEntities(template1, template2);

        when(executer.post("/templates/_search?size=50", "{\n" +
                "\"_source\": [\"hesnamespace\", \"name\"],\n" +
                "\"query\": {\n" +
                "\"bool\": {\n" +
                "\"must\": [\n" +
                "    {\n" +
                "    \"wildcard\": {\n" +
                "    \"hesnamespace.namespace\": \"*token1*\"\n" +
                "    }\n" +
                "    },\n" +
                "    {\n" +
                "    \"wildcard\": {\n" +
                "    \"hesnamespace.namespace\": \"*token2*\"\n" +
                "    }\n" +
                "    },\n" +
                "    {\n" +
                "    \"wildcard\": {\n" +
                "    \"hesnamespace.namespace\": \"*token3*\"\n" +
                "    }\n" +
                "    }\n" +
                "]\n" +
                "}\n" +
                "},\n" +
                "\"from\": 0,\n" +
                "\"size\": 100,\n" +
                "\"sort\": []\n" +
                "}"))
                .thenReturn(elasticSearchResponse);

        Set<TemplateSearchResponse> templates = templateSearch.getTemplatesByNamespaceLike(new String[]{"*token1*", "*token2*", "*token3*"});
        assertThat(templates.size()).isEqualTo(2);
        assertThat(templates.contains(template1)).isTrue();
        assertThat(templates.contains(template2)).isTrue();
    }

    @Test
    public void search_by_namespace_should_be_lowercase() throws IOException {
        TemplateSearchResponse template1 = new TemplateSearchResponse("some_namespace", "t1");
        TemplateSearchResponse template2 = new TemplateSearchResponse("some_namespace", "t2");
        ElasticSearchResponse<TemplateSearchResponse> elasticSearchResponse = elasticSearchResponseWithEntities(template1, template2);

        when(executer.post("/templates/_search?size=50", "{\n" +
                "\"_source\": [\"hesnamespace\", \"name\"],\n" +
                "\"query\": {\n" +
                "\"bool\": {\n" +
                "\"must\": [\n" +
                "    {\n" +
                "    \"wildcard\": {\n" +
                "    \"hesnamespace.namespace\": \"*token1*\"\n" +
                "    }\n" +
                "    },\n" +
                "    {\n" +
                "    \"wildcard\": {\n" +
                "    \"hesnamespace.namespace\": \"*token2*\"\n" +
                "    }\n" +
                "    },\n" +
                "    {\n" +
                "    \"wildcard\": {\n" +
                "    \"hesnamespace.namespace\": \"*token3*\"\n" +
                "    }\n" +
                "    }\n" +
                "]\n" +
                "}\n" +
                "},\n" +
                "\"from\": 0,\n" +
                "\"size\": 100,\n" +
                "\"sort\": []\n" +
                "}"))
                .thenReturn(elasticSearchResponse);

        Set<TemplateSearchResponse> templates = templateSearch.getTemplatesByNamespaceLike(new String[]{"*TOKEN1*", "*toKEn2*", "*token3*"});
        assertThat(templates.size()).isEqualTo(2);
        assertThat(templates.contains(template1)).isTrue();
        assertThat(templates.contains(template2)).isTrue();
    }

}
