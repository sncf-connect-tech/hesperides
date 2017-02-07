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
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static com.vsct.dt.hesperides.ElasticSearchMock.elasticSearchResponseWithEntities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by william_montaz on 09/10/2014.
 */
public class ApplicationSearchTest {

    final private ElasticSearchClient elasticSearchClient = mock(ElasticSearchClient.class);
    final private ElasticSearchClient.RequestExecuter executer = mock(ElasticSearchClient.RequestExecuter.class);
    private ApplicationSearch applicationSearch;

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Before
    public void resetMock() {
        reset(elasticSearchClient);
        reset(executer);
        when(elasticSearchClient.withResponseReader(any())).thenReturn(executer);
        applicationSearch = new ApplicationSearch(elasticSearchClient);
    }

    @Test
    public void testGetApplicationsLike() throws IOException {
        ApplicationSearchResponse app1 = new ApplicationSearchResponse("application_name1");
        ApplicationSearchResponse app2 = new ApplicationSearchResponse("application_name2");

        ElasticSearchResponse<ApplicationSearchResponse> elasticSearchResponse = elasticSearchResponseWithEntities(app1, app2);
        when(executer.post("/platforms/_search?size=50", "{\n" +
                "\"_source\": [\"application_name\"],\n" +
                "\"query\": {\n" +
                "\"wildcard\": {\n" +
                "\"platforms.application_name\": \"*application*\"\n" +
                "}\n" +
                "}\n" +
                "}"))
                .thenReturn(elasticSearchResponse);

        Set<ApplicationSearchResponse> applications = applicationSearch.getApplicationsLike("application");

        assertThat(applications.size()).isEqualTo(2);
        assertThat(applications.contains(app1)).isTrue();
        assertThat(applications.contains(app2)).isTrue();
    }

    @Test
    public void testgetAllPlatforms() throws IOException {
        PlatformSearchResponse app1 = new PlatformSearchResponse("platform_name1");
        PlatformSearchResponse app2 = new PlatformSearchResponse("platform_name2");

        ElasticSearchResponse<PlatformSearchResponse> elasticSearchResponse = elasticSearchResponseWithEntities(app1, app2);

        when(executer.post(anyString(), anyString())).thenReturn(elasticSearchResponse);

        Set<PlatformSearchResponse> applications = applicationSearch.getAllPlatforms("application", "");

        assertThat(applications.size()).isEqualTo(2);
        assertThat(applications.contains(app1)).isTrue();
        assertThat(applications.contains(app2)).isTrue();
    }

}
