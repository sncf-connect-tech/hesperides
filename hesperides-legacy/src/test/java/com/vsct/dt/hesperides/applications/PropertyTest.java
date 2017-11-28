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

import com.github.mustachejava.codes.DefaultCode;
import com.vsct.dt.hesperides.templating.models.Property;
import com.vsct.dt.hesperides.templating.models.exception.ModelAnnotationException;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tests.type.UnitTests;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by william_montaz on 01/09/14.
 */
@Category(UnitTests.class)
public class PropertyTest {

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
            assertThat(e.getMessage()).isEqualTo("Many annotation @comment for property 'codeName'");
        }

        try {
            code = new DefaultCode();
            f.set(code, "codeName|@comment true @comment false");
            new Property(code);
            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Many annotation @comment for property 'codeName'");
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

        code = new DefaultCode();
        f.set(code, "codeName|@required @required");
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

        code = new DefaultCode();
        f.set(code, "codeName|Some comment @default \"valeurParDefaut 2\" invalid");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.getDefaultValue()).isEqualTo("valeurParDefaut 2");

        code = new DefaultCode();
        f.set(code, "codeName|Some comment @default \"valeurParDefaut \\\"2\"");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.getDefaultValue()).isEqualTo("valeurParDefaut \"2");

        try {
            code = new DefaultCode();
            f.set(code, "codeName|Some comment @default \"valeurParDefaut");
            new Property(code);

            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Annotation '@default' is not valid for property 'codeName'. Please check it !");
        }

        try {
            code = new DefaultCode();
            f.set(code, "codeName|Some comment @default \"valeurParDefaut\\");
            new Property(code);
            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Annotation '@default' is not valid for property 'codeName'. Please check it !");
        }

        try {
            code = new DefaultCode();
            f.set(code, "codeName|Some comment @default true @default false");
            new Property(code);
            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Many annotation @default for property 'codeName'");
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
            assertThat(e.getMessage()).isEqualTo("Many annotation @pattern for property 'codeName'");
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

        code = new DefaultCode();
        f.set(code, "codeName|foo @ffffff foo @@ @ere@ @e@@@@@ ezr@@ ezrzerze@e@e@e@e@e@e@@");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.getPattern()).isEmpty();

        code = new DefaultCode();
        f.set(code, "codeName|foo @ffffff foo @@ @ere@ @e@@@@@ ezr@@ ezrzerze@e@e@e@e@e@e@@ @default true");
        hesperidesProperty = new Property(code);
        assertThat(hesperidesProperty.getDefaultValue()).isEqualTo("true");

        try {
            code = new DefaultCode();
            f.set(code, "codeName|foo @ffffff @required @e @rtrtr @ trriut@@@@");
            new Property(code);
            fail("Need raise error");
        } catch (ModelAnnotationException e) {
            assertThat(e.getMessage()).isEqualTo("Invalid annotation name at 32 for property 'codeName' with annotation '@e' !");
        }
    }
}
