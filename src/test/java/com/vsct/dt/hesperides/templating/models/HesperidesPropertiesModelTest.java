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
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.codes.DefaultMustache;
import com.google.common.collect.Sets;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

import com.vsct.dt.hesperides.templating.models.exception.ModelAnnotationException;

import static com.vsct.dt.hesperides.TestUtils.flattenJSON;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;


/**
 * Created by william_montaz on 02/09/14.
 */
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
    public void shouldCreateModelFromCode() {
        Property prop = createPropertyFromString("x|@default \"default\" @pattern \"pattern\" @password @comment \"comment\"");
        assertThat("default").isEqualTo(prop.getDefaultValue());
        assertThat("pattern").isEqualTo(prop.getPattern());
        assertThat(prop.isPassword()).isTrue();
        assertThat("comment").isEqualTo(prop.getComment());
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

    @Test
    public void requiredAndDefaultValueFail() {
        try {
            createPropertyFromString("x|@default \"DEFAULT\" @required");
            failBecauseExceptionWasNotThrown(ModelAnnotationException.class);
        } catch (ModelAnnotationException error) {
            assertThat(error).hasMessage("Property 'x' canno't be @required and @default");
        }
    }

    @Test
    public void emptyDefaultOrPatternFail() {
        try {
            createPropertyFromString("x|@default \"\"");
            failBecauseExceptionWasNotThrown(ModelAnnotationException.class);
        } catch (ModelAnnotationException error) {
            assertThat(error).hasMessage("Annotation '@default' for property 'x' cannot be empty");
        }
        try {
            createPropertyFromString("x|@pattern \"\"");
            failBecauseExceptionWasNotThrown(ModelAnnotationException.class);
        } catch (ModelAnnotationException error) {
            assertThat(error).hasMessage("Annotation '@pattern' for property 'x' cannot be empty");
        }
    }

    @Test
    public void duplicateAnnotationFail() {
        try {
            createPropertyFromString("x|@required @required");
            failBecauseExceptionWasNotThrown(ModelAnnotationException.class);
        } catch (ModelAnnotationException error) {
            assertThat(error).hasMessage("Duplicate annotations '@required' found for property 'x'");
        }
    }

    @Test
    public void extraneousAnnotationValueFail() {
        try {
            createPropertyFromString("x|@required \"foo\"");
            failBecauseExceptionWasNotThrown(ModelAnnotationException.class);
        } catch (ModelAnnotationException error) {
            assertThat(error).hasMessage("Annotation '@required' is not valid for property 'x': you probably provided a string value to an annotation that is simply a flag");
        }
    }

    private Property createPropertyFromString(String template) {
        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        DefaultMustache defaultMustache = (DefaultMustache)mustacheFactory.compile(new StringReader(template), template);
        return new Property(defaultMustache);
    }
}
