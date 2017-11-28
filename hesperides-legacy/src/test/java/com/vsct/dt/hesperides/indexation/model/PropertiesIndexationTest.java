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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tests.type.UnitTests;

import java.io.IOException;
import java.util.Set;

import static com.vsct.dt.hesperides.TestUtils.flattenJSON;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by william_montaz on 01/09/14.
 */
@Category(UnitTests.class)
public class PropertiesIndexationTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void shouldSerializeToJSON() throws IOException {
        /* Create the KeyValueProperty */
        KeyValuePropertyIndexation kvp = new KeyValuePropertyIndexation("name", "value", "some comment");

        /* Create the iterable property */
        PropertyIndexation propertyIndexation = new PropertyIndexation("name1", "comment1");
        Set<PropertyIndexation> fields = Sets.newHashSet(propertyIndexation);

        IterablePropertyIndexation.Valorisation field = new IterablePropertyIndexation.Valorisation("blockOfProperties", Sets.newHashSet(new KeyValuePropertyIndexation("name2", "value", "comment2")));

        IterablePropertyIndexation ip = new IterablePropertyIndexation("iterable", "some comment", fields, Lists.newArrayList(field));

        PropertiesIndexation hesperidesPropertiesIndexation = new PropertiesIndexation("properties.UNI.0.pltfm.unit", Sets.newHashSet(kvp), Sets.newHashSet(ip));

        assertThat(MAPPER.writeValueAsString(hesperidesPropertiesIndexation)).isEqualTo(flattenJSON("fixtures/indexation/properties.json"));
    }

    @Test
    public void shouldDeserializeFromJSON() throws IOException {
        /* Create the KeyValueProperty */
        KeyValuePropertyIndexation kvp = new KeyValuePropertyIndexation("name", "value", "some comment");

        /* Create the iterable property */
        PropertyIndexation propertyIndexation = new PropertyIndexation("name1", "comment1");
        Set<PropertyIndexation> fields = Sets.newHashSet(propertyIndexation);

        IterablePropertyIndexation.Valorisation field = new IterablePropertyIndexation.Valorisation("blockOfProperties", Sets.newHashSet(new KeyValuePropertyIndexation("name2", "value", "comment2")));

        IterablePropertyIndexation ip = new IterablePropertyIndexation("iterable", "some comment", fields, Lists.newArrayList(field));

        PropertiesIndexation hesperidesPropertiesIndexation = new PropertiesIndexation("properties.UNI.0.pltfm.unit", Sets.newHashSet(kvp), Sets.newHashSet(ip));

        assertThat(DeepEquals.deepEquals(MAPPER.readValue(fixture("fixtures/indexation/properties.json"), PropertiesIndexation.class), hesperidesPropertiesIndexation)).isTrue();
    }

}
