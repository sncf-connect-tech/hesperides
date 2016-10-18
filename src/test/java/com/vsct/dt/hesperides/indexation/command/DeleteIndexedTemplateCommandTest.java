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

package com.vsct.dt.hesperides.indexation.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsct.dt.hesperides.indexation.ElasticSearchClient;
import com.vsct.dt.hesperides.indexation.model.TemplateIndexation;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by william_montaz on 11/02/2015.
 */
public class DeleteIndexedTemplateCommandTest {

    final private        ElasticSearchClient                 elasticSearchClient = mock(ElasticSearchClient.class);
    final private        ElasticSearchClient.RequestExecuter executer            = mock(ElasticSearchClient.RequestExecuter.class);
    private static final ObjectMapper                        MAPPER              = Jackson.newObjectMapper();

    @Before
         public void resetMock() {
        reset(elasticSearchClient);
        reset(executer);
        when(elasticSearchClient.withResponseReader(any())).thenReturn(executer);
    }

    @Test
    public void shouldCallDeleteOnTemplatesUrlUsingTemplateId() throws IOException {
        TemplateIndexation hesperidesTemplate = new TemplateIndexation("namespace", "name", null, null);

        new DeleteIndexedTemplateCommand("namespace", "name").index(elasticSearchClient);

        verify(executer).delete("/templates/" + hesperidesTemplate.getId());
    }
}
