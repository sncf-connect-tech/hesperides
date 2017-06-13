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

package com.vsct.dt.hesperides.applications;

import com.cedarsoftware.util.DeepEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsct.dt.hesperides.resources.KeyValueValorisation;
import io.dropwizard.jackson.Jackson;
import tests.type.UnitTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

import static com.vsct.dt.hesperides.TestUtils.flattenJSON;
import static io.dropwizard.testing.FixtureHelpers.fixture;

/**
 * Created by william_montaz on 01/09/14.
 */
@Category(UnitTests.class)
public class KeyValueValorisationTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void shouldSerializeToJSON() throws IOException {
        KeyValueValorisation kvp = new KeyValueValorisation("the name", "some value");

        assertThat(MAPPER.writeValueAsString(kvp)).isEqualTo(flattenJSON("fixtures/business/key_value_property.json"));
    }

    @Test
    public void shouldDeserializeFromJSON() throws IOException {
        KeyValueValorisation kvp = new KeyValueValorisation("the name", "some value");

        assertThat(DeepEquals.deepEquals(MAPPER.readValue(fixture("fixtures/business/key_value_property.json"), KeyValueValorisation.class), kvp)).isTrue();
    }

}
