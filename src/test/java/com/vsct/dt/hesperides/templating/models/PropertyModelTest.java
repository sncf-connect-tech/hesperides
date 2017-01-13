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
import com.github.mustachejava.codes.DefaultCode;
import com.github.mustachejava.codes.DefaultMustache;
import com.google.common.collect.Sets;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Set;

import com.vsct.dt.hesperides.templating.models.exception.ModelAnnotationException;

import static com.vsct.dt.hesperides.TestUtils.flattenJSON;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;


public class PropertyModelTest {

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
        assertThat(prop.getDefaultValue()).isEqualTo("default");
        assertThat(prop.getPattern()).isEqualTo("pattern");
        assertThat(prop.isPassword()).isTrue();
        assertThat(prop.getComment()).isEqualTo("comment");
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
    public void missingWhitespacesFail() {
        try {
            createPropertyFromString("toto|@default\"ola\"@required");
            failBecauseExceptionWasNotThrown(ModelAnnotationException.class);
        } catch (ModelAnnotationException error) {
            assertThat(error).hasMessage("Unknown annotation 'default\"ola\"@required' for property 'toto'");
        }
    }

    @Test
    public void unknownAnnotationFail() {
        try {
            createPropertyFromString("toto|@qsdsq");
            failBecauseExceptionWasNotThrown(ModelAnnotationException.class);
        } catch (ModelAnnotationException error) {
            assertThat(error).hasMessage("Unknown annotation 'qsdsq' for property 'toto'");
        }
        try {
            createPropertyFromString("toto|@required @");
            failBecauseExceptionWasNotThrown(ModelAnnotationException.class);
        } catch (ModelAnnotationException error) {
            assertThat(error).hasMessage("Unknown annotation '' for property 'toto'");
        }
    }

    @Test
    public void emptyPropertySecondFieldFail() {
        try {
            createPropertyFromString("toto|  ");
            failBecauseExceptionWasNotThrown(ModelAnnotationException.class);
        } catch (ModelAnnotationException error) {
            assertThat(error).hasMessage("Property 'toto' has a second field without any annotation");
        }
    }

    @Test
    public void extraneousAnnotationValueFail() {
        try {
            createPropertyFromString("x|@required \"foo\"");
            failBecauseExceptionWasNotThrown(ModelAnnotationException.class);
        } catch (ModelAnnotationException error) {
            assertThat(error).hasMessage("Invalid annotation: expected character '@' but found '\"foo\"' for property 'x'");
        }
    }

    private Property createPropertyFromString(String template) {
        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        DefaultMustache defaultMustache = (DefaultMustache)mustacheFactory.compile(new StringReader(template), template);
        return new Property(defaultMustache);
    }

    @Test
    public void shouldConstructHesperidesPropertyFromMustacheCode() throws NoSuchFieldException, IllegalAccessException {
        Field f = DefaultCode.class.getDeclaredField("name");
        f.setAccessible(true);

        DefaultCode code = new DefaultCode();
        f.set(code, "codeName");

        Property hesperidesProperty = new Property(code);

        assertThat(hesperidesProperty.getName()).isEqualTo("codeName");
    }

    @Test
    public void shouldConstructHesperidesPropertyFromMustacheCodeWithComment() throws NoSuchFieldException, IllegalAccessException {
        Field f = DefaultCode.class.getDeclaredField("name");
        f.setAccessible(true);

        DefaultCode code = new DefaultCode();
        f.set(code, "codeName|Some comment");

        Property hesperidesProperty = new Property(code);

        assertThat(hesperidesProperty.getName()).isEqualTo("codeName");
        assertThat(hesperidesProperty.getComment()).isEqualTo("Some comment");

        try {
            code = new DefaultCode();
            f.set(code, "codeName|Some comment @comment true @comment false");
            new Property(code);
            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Duplicate annotations '@comment' found for property 'codeName'");
        }

        try {
            code = new DefaultCode();
            f.set(code, "codeName|@comment true @comment false");
            new Property(code);
            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Duplicate annotations '@comment' found for property 'codeName'");
        }
    }

    @Test
    public void shouldConstructHesperidesPropertyFromMustacheCodeWithRequiredAttribute() throws NoSuchFieldException, IllegalAccessException {
        Field f = DefaultCode.class.getDeclaredField("name");
        f.setAccessible(true);

        DefaultCode code;
        Property hesperidesProperty;

        code = new DefaultCode();
        f.set(code, "codeName|Some comment");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.isRequired()).isFalse();

        code = new DefaultCode();
        f.set(code, "codeName|Some comment @required");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.isRequired()).isTrue();
    }

    @Test
    public void shouldConstructHesperidesPropertyFromMustacheCodeWithDefaultAttribute() throws NoSuchFieldException, IllegalAccessException {
        Field f = DefaultCode.class.getDeclaredField("name");
        f.setAccessible(true);

        DefaultCode code;
        Property hesperidesProperty;

        code = new DefaultCode();
        f.set(code, "codeName|Some comment");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.getDefaultValue()).isEmpty();

        code = new DefaultCode();
        f.set(code, "codeName|Some comment @default valeurParDefaut");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.getDefaultValue()).isEqualTo("valeurParDefaut");

        code = new DefaultCode();
        f.set(code, "codeName|Some comment @default 'valeurParDefaut'");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.getDefaultValue()).isEqualTo("valeurParDefaut");

        code = new DefaultCode();
        f.set(code, "codeName|Some comment @default \"valeurParDefaut 2\"");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.getDefaultValue()).isEqualTo("valeurParDefaut 2");

        try {
            code = new DefaultCode();
            f.set(code, "codeName|Some comment @default \"valeurParDefaut 2\" invalid");
            new Property(code);

            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Invalid annotation: expected character '@' but found 'invalid' for property 'codeName'");
        }

        code = new DefaultCode();
        f.set(code, "codeName|Some comment @default \"valeurParDefaut \\\"2\"");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.getDefaultValue()).isEqualTo("valeurParDefaut \"2");

        try {
            code = new DefaultCode();
            f.set(code, "codeName|Some comment @required TRUE");
            new Property(code);

            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Invalid annotation: expected character '@' but found 'TRUE' for property 'codeName'");
        }

        try {
            code = new DefaultCode();
            f.set(code, "codeName|Some comment @default \"valeurParDefaut");
            new Property(code);

            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Non-closing '\"'-escaped value found for annotation 'default' in property 'codeName'");
        }

        try {
            code = new DefaultCode();
            f.set(code, "codeName|Some comment @default \"valeurParDefaut\\");
            new Property(code);
            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Non-closing '\"'-escaped value found for annotation 'default' in property 'codeName'");
        }

        try {
            code = new DefaultCode();
            f.set(code, "codeName|Some comment @default true @default false");
            new Property(code);
            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Duplicate annotations '@default' found for property 'codeName'");
        }
    }

    @Test
    public void shouldConstructHesperidesPropertyFromMustacheCodeWithPatternAttribute() throws NoSuchFieldException, IllegalAccessException {
        Field f = DefaultCode.class.getDeclaredField("name");
        f.setAccessible(true);

        DefaultCode code;
        Property hesperidesProperty;

        code = new DefaultCode();
        f.set(code, "codeName|Some comment");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.getPattern()).isEmpty();

        code = new DefaultCode();
        f.set(code, "codeName|Some comment @pattern a");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.getPattern()).isEqualTo("a");

        code = new DefaultCode();
        f.set(code, "codeName|Some comment @pattern a|b");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.getPattern()).isEqualTo("a|b");

        code = new DefaultCode();
        f.set(code, "codeName|Some comment @pattern 'a!b'");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.getPattern()).isEqualTo("a!b");

        try {
            code = new DefaultCode();
            f.set(code, "codeName|@pattern true @pattern false");
            new Property(code);
            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Duplicate annotations '@pattern' found for property 'codeName'");
        }
    }

    @Test
    public void shouldConstructHesperidesPropertyFromMustacheCodeWithArobaseInComment() throws NoSuchFieldException, IllegalAccessException {
        Field f = DefaultCode.class.getDeclaredField("name");
        f.setAccessible(true);

        DefaultCode code;
        Property hesperidesProperty;

        code = new DefaultCode();
        f.set(code, "codeName|Some comment email@truc.com");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.getPattern()).isEmpty();

        try {
            code = new DefaultCode();
            f.set(code, "codeName|foo @ffffff foo @@ @ere@ @e@@@@@ ezr@@ ezrzerze@e@e@e@e@e@e@@ @default true");
            new Property(code);
            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Unknown annotation 'ffffff' for property 'codeName'");
        }

        try {
            code = new DefaultCode();
            f.set(code, "codeName|foo @ffffff @required @e @rtrtr @ trriut@@@@");
            new Property(code);
            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Unknown annotation 'ffffff' for property 'codeName'");
        }
    }
}
