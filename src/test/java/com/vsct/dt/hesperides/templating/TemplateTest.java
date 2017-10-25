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

package com.vsct.dt.hesperides.templating;

import com.cedarsoftware.util.DeepEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import io.dropwizard.jackson.Jackson;
import tests.type.UnitTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

import static com.vsct.dt.hesperides.TestUtils.flattenJSON;
import static io.dropwizard.testing.FixtureHelpers.fixture;

/**
 * Created by william_montaz on 29/08/14.
 */
@Category(UnitTests.class)
public class TemplateTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void shouldSerializeToJSON() throws IOException {
        final Template hesperidesTemplate = new Template("technos.nodejs.0", "instance.sh", "{{INSTANCE_NAME}}.sh", "{{HOME}}/{{INSTANCE_NAME}}", "some_content", null, 1L);

        assertThat(MAPPER.writeValueAsString(hesperidesTemplate)).isEqualTo(flattenJSON("fixtures/business/template.json"));
    }

    @Test
    public void shouldDeserializeFromJSON() throws IOException {
        final Template hesperidesTemplate = new Template("technos.nodejs.0", "instance.sh", "{{INSTANCE_NAME}}.sh", "{{HOME}}/{{INSTANCE_NAME}}", "some_content", null, 1L);

        assertThat(DeepEquals.deepEquals(MAPPER.readValue(fixture("fixtures/business/template.json"), Template.class), hesperidesTemplate)).isTrue();
    }

    @Test
    public void shouldGeneratePropertiesModel() {
        /* TemplateSlurper is tested somewhere else, here we only test that PropertiesModel embraces All fields */
        final Template hesperidesTemplate = new Template("technos.nodejs.0", "instance.sh", "{{prop_filename}}", "{{prop_location}}", "{{prop_content}};withSomeWhiteSpace={{  prop_with_whitespaces    }};withSomeTabs = {{\t\t   prop_with_some_tabs_at_start_and_end \t  }}", null, 1L);

        HesperidesPropertiesModel model = hesperidesTemplate.generatePropertiesModel();

        assertThat(model.hasProperty("prop_content")).isTrue();
        assertThat(model.hasProperty("prop_filename")).isTrue();
        assertThat(model.hasProperty("prop_location")).isTrue();

        // Testing clean property names
        assertThat(model.hasProperty("prop_with_whitespaces"));
        assertThat(model.hasProperty("prop_with_some_tabs_at_start_and_end")).isTrue();
    }

    static String readFile(String path)
            throws IOException {
        return new String(ByteStreams.toByteArray(TemplateTest.class.getClassLoader().getResourceAsStream(path)), "UTF-8");
    }



}
