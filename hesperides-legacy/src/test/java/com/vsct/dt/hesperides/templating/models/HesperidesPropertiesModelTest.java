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

package com.vsct.dt.hesperides.templating.models;

import com.cedarsoftware.util.DeepEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import io.dropwizard.jackson.Jackson;
import tests.type.UnitTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.Set;

import static com.vsct.dt.hesperides.TestUtils.flattenJSON;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by william_montaz on 02/09/14.
 */
@Category(UnitTests.class)
public class HesperidesPropertiesModelTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void shouldSerializeToJSON() throws IOException {
        /* Create the KeyValueProperty */
        KeyValuePropertyModel kvp = new KeyValuePropertyModel("name", "some comment");

        /* Create the iterable property */
        Property property = new Property("name", "comment");
        Set<Property> fields = Sets.newHashSet(property);
        IterablePropertyModel ip = new IterablePropertyModel("iterable", "some comment", fields);


        HesperidesPropertiesModel model = new HesperidesPropertiesModel(Sets.newHashSet(kvp), Sets.newHashSet(ip));

        assertThat(MAPPER.writeValueAsString(model)).isEqualTo(flattenJSON("fixtures/business/hesperides_properties_model.json"));
    }

    @Test
    public void shouldDeserializeFromJSON() throws IOException {
       /* Create the KeyValueProperty */
        KeyValuePropertyModel kvp = new KeyValuePropertyModel("name", "some comment");

        /* Create the iterable property */
        Property property = new Property("name", "comment");
        Set<Property> fields = Sets.newHashSet(property);
        IterablePropertyModel ip = new IterablePropertyModel("iterable", "some comment", fields);

        HesperidesPropertiesModel model = new HesperidesPropertiesModel(Sets.newHashSet(kvp), Sets.newHashSet(ip));

        assertThat(DeepEquals.deepEquals(MAPPER.readValue(fixture("fixtures/business/hesperides_properties_model.json"), HesperidesPropertiesModel.class), model));
    }

    @Test
    public void shouldCreateModelFromCode() throws NoSuchFieldException, IllegalAccessException {
        /* HARD to create mustache objetcs (constructors, private fields, etc...) */
    }

    @Test
    public void shouldAddIterableFromCode() {
        /* HARD TO create mustache Objects constructors, private fields, etc...) */
    }

    @Test
    public void mergeShouldAddAllPropertiesHavingADifferentNameAndNotChangeTheExistingOnes() {
        /* Create the KeyValueProperty */
        KeyValuePropertyModel kvp1A = new KeyValuePropertyModel("kvp1", "some comment");

        KeyValuePropertyModel kvp2 = new KeyValuePropertyModel("kvp2", "kvp2 comment");

        /* Create the iterable property */
        IterablePropertyModel ip1A = new IterablePropertyModel("iterable1", "some comment", Sets.newHashSet());

        IterablePropertyModel ip2 = new IterablePropertyModel("iterable2", "some different comment", Sets.newHashSet());

        HesperidesPropertiesModel hesperidesPropertiesModelFromA = new HesperidesPropertiesModel(Sets.newHashSet(kvp1A), Sets.newHashSet(ip1A));

        HesperidesPropertiesModel hesperidesPropertiesModelFromB = new HesperidesPropertiesModel(Sets.newHashSet(kvp2), Sets.newHashSet(ip2));

        HesperidesPropertiesModel afterMerge = new HesperidesPropertiesModel(Sets.newHashSet(kvp1A, kvp2), Sets.newHashSet(ip1A, ip2));

        HesperidesPropertiesModel merged = hesperidesPropertiesModelFromA.merge(hesperidesPropertiesModelFromB);

        assertThat(merged).isEqualTo(afterMerge);
    }


}
