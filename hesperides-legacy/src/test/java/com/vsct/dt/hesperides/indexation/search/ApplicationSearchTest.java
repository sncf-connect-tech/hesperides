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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by william_montaz on 09/10/2014.
 */
@Category(UnitTests.class)
public class ApplicationSearchTest {
    public static final int       SEARCH_SIZE     = 50;

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
        ApplicationSearchResponse app1 = new ApplicationSearchResponse("application_name1", "application_version1", "platform_name1");
        ApplicationSearchResponse app2 = new ApplicationSearchResponse("application_name2", "application_version2", "platform_name2");

        ElasticSearchResponse<ApplicationSearchResponse> elasticSearchResponse = elasticSearchResponseWithEntities(app1, app2);
        when(executer.post("/platforms/_search?size=50",
                "{\n" +
                        "   \"_source\":[\n" +
                        "      \"application_name\"\n" +
                        "   ],\n" +
                        "   \"query\":{\n" +
                        "      \"wildcard\":{\n" +
                        "         \"platforms.application_name\":\"*application*\"\n" +
                        "      }\n" +
                        "   }\n" +
                        "}"
                ))
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

    @Test
    public void testgetAllPlatformsUsingModulesWithHyphenInNameAndReleaseType() throws IOException {
        String url = String.format("/platforms/_search?size=%1$s", SEARCH_SIZE);

        PlatformApplicationSearchResponse appHyphenRelease1
                = new PlatformApplicationSearchResponse("application_name1", "platform_name1");
        PlatformApplicationSearchResponse appHyphenRelease2
                = new PlatformApplicationSearchResponse("application_name2", "platform_name2");

        ElasticSearchResponse<PlatformApplicationSearchResponse> elasticSearchResponseHyphenRelease
                = elasticSearchResponseWithEntities(appHyphenRelease1, appHyphenRelease2);

        String bodyHyphenRelease = "{\n" +
                "\"_source\": [\"application_name\", \"platform_name\"],\n" +
                "\"query\":{\n" +
                "\"constant_score\":{\n" +
                "\"filter\":{\n" +
                "\"bool\":{\n" +
                "\"must\":[\n" +
                "{\n" +
                "\"term\":{\n" +
                "\"modules.name\":\"moduleName\"\n" +
                "}\n" +
                "},\n" +
                "{\n" +
                "\"term\":{\n" +
                "\"modules.version\" : \"1.0.0.0-SNAPSHOT\"\n" +
                "}\n" +
                "},\n" +
                "{\n" +
                "\"term\":{\n" +
                "\"modules.working_copy\": false\n" +
                "}\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}";

        when(executer.post(url, bodyHyphenRelease))
                .thenReturn(elasticSearchResponseHyphenRelease);

        Set<PlatformApplicationSearchResponse> applicationsReleaseSnapshot
                = applicationSearch.getAllPlatformsUsingModules("moduleName", "1.0.0.0-SNAPSHOT", "release");

        assertThat(applicationsReleaseSnapshot.size()).isEqualTo(2);
        assertThat(applicationsReleaseSnapshot.contains(appHyphenRelease1)).isTrue();
        assertThat(applicationsReleaseSnapshot.contains(appHyphenRelease2)).isTrue();
    }

    @Test
    public void testgetAllPlatformsUsingModulesWithWorkingCopyType() throws IOException {
        String url = String.format("/platforms/_search?size=%1$s", SEARCH_SIZE);

        PlatformApplicationSearchResponse appWorkingCopy1
                = new PlatformApplicationSearchResponse("application_name1", "platform_name1");
        PlatformApplicationSearchResponse appWorkingCopy2
                = new PlatformApplicationSearchResponse("application_name2", "platform_name2");

        ElasticSearchResponse<PlatformApplicationSearchResponse> elasticSearchResponseWorkingCopy
                = elasticSearchResponseWithEntities(appWorkingCopy1, appWorkingCopy2);

        String bodyWorkingCopy = "{\n" +
                "\"_source\": [\"application_name\", \"platform_name\"],\n" +
                "\"query\":{\n" +
                "\"constant_score\":{\n" +
                "\"filter\":{\n" +
                "\"bool\":{\n" +
                "\"must\":[\n" +
                "{\n" +
                "\"term\":{\n" +
                "\"modules.name\":\"moduleName\"\n" +
                "}\n" +
                "},\n" +
                "{\n" +
                "\"term\":{\n" +
                "\"modules.version\" : \"1.0.0.0\"\n" +
                "}\n" +
                "},\n" +
                "{\n" +
                "\"term\":{\n" +
                "\"modules.working_copy\": true\n" +
                "}\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}";

        when(executer.post(url, bodyWorkingCopy))
                .thenReturn(elasticSearchResponseWorkingCopy);

        Set<PlatformApplicationSearchResponse> applicationsWorkingCopy
                = applicationSearch.getAllPlatformsUsingModules("moduleName", "1.0.0.0", "workingcopy");

        assertThat(applicationsWorkingCopy.size()).isEqualTo(2);
        assertThat(applicationsWorkingCopy.contains(appWorkingCopy1)).isTrue();
        assertThat(applicationsWorkingCopy.contains(appWorkingCopy2)).isTrue();
    }

}
