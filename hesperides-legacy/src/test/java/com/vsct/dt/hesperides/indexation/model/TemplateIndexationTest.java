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

package com.vsct.dt.hesperides.indexation.model;

import com.cedarsoftware.util.DeepEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tests.type.UnitTests;

import java.io.IOException;

import static com.vsct.dt.hesperides.TestUtils.flattenJSON;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by william_montaz on 29/08/14.
 */
@Category(UnitTests.class)
public class TemplateIndexationTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void shouldSerializeToJSON() throws IOException {
        final TemplateIndexation hesperidesTemplateIndexation = new TemplateIndexation(
                "technos.nodejs.0",
                "instance.sh",
                "{{INSTANCE_NAME}}.sh",
                "{{HOME}}/{{INSTANCE_NAME}}"
        );

        assertThat(MAPPER.writeValueAsString(hesperidesTemplateIndexation)).isEqualTo(flattenJSON("fixtures/indexation/template.json"));
    }

    @Test
    public void shouldDeserializeFromJSON() throws IOException {
        final TemplateIndexation hesperidesTemplateIndexation = new TemplateIndexation(
                "technos.nodejs.0",
                "instance.sh",
                "{{INSTANCE_NAME}}.sh",
                "{{HOME}}/{{INSTANCE_NAME}}"
        );

        assertThat(DeepEquals.deepEquals(MAPPER.readValue(fixture("fixtures/indexation/template.json"), TemplateIndexation.class), hesperidesTemplateIndexation)).isTrue();
    }

}
