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

package com.vsct.dt.hesperides.templating.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tests.type.UnitTests;

import java.io.IOException;

import static com.vsct.dt.hesperides.TestUtils.flattenJSON;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by william_montaz on 20/02/2015.
 */
@Category(UnitTests.class)
public class ModuleClientTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void serializesToJSON() throws IOException {
        Techno techno = new Techno("name", "version", true);
        Module module = new Module(
                "my_module",
                "the_version",
                true,
                Sets.newHashSet(techno),
                2L
        );

        assertThat(MAPPER.writeValueAsString(module)).isEqualTo(flattenJSON("fixtures/business/module.json"));

    }

    @Test
    public void shouldDeserializeFromJSON() throws IOException {
        Techno techno = new Techno("name", "version", true);
        Module module = new Module(
                "my_module",
                "the_version",
                true,
                Sets.newHashSet(techno),
                2L
        );

        assertThat(MAPPER.readValue(fixture("fixtures/business/module.json"), Module.class)).isEqualTo(module);
    }
}
