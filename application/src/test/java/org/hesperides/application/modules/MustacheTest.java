/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.application.modules;

import com.github.mustachejava.Code;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.codes.ValueCode;
import org.hesperides.domain.templatecontainer.queries.ModelView;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class MustacheTest {

    @Test
    public void testExtractKeyValueProperties() {
        List<ModelView.PropertyView> keyValueProperties = extractKeyValueProperties("{{ foo}} {{bar }} {{ fub }}");
        assertEquals(3, keyValueProperties.size());
        assertEquals("foo", keyValueProperties.get(0).getName());
        assertEquals("bar", keyValueProperties.get(1).getName());
        assertEquals("fub", keyValueProperties.get(2).getName());
    }

    @Test
    public void testExtractKeyValueProperty() {
        ModelView.PropertyView completePropertyView = extractKeyValueProperty("foo|@required|@comment \"comment\"|@default 5|@pattern \"pattern\"|@password");
        assertEquals("foo", completePropertyView.getName());
        assertEquals(true, completePropertyView.isRequired());
        assertEquals("comment", completePropertyView.getComment());
        assertEquals("5", completePropertyView.getDefaultValue());
        assertEquals("pattern", completePropertyView.getPattern());
        assertEquals(true, completePropertyView.isPassword());

        ModelView.PropertyView minimalistPropertyView = extractKeyValueProperty("bar");
        assertEquals("bar", minimalistPropertyView.getName());
        assertEquals(false, minimalistPropertyView.isRequired());
        assertEquals("", minimalistPropertyView.getComment());
        assertEquals("", minimalistPropertyView.getDefaultValue());
        assertEquals("", minimalistPropertyView.getPattern());
        assertEquals(false, minimalistPropertyView.isPassword());
    }

    @Test
    public void testExtractVariableOptionValue() {
        assertEquals("something without any quotes", extractVariableOptionValue("@anyOption something without any quotes"));
        assertEquals("something with quotes", extractVariableOptionValue(" @anyOption \"something with quotes\" "));
        assertEquals("12", extractVariableOptionValue("@anyOption 12"));
        assertEquals("something else", extractVariableOptionValue("   something that should not be there   @anyOption   something else      "));
    }

    @Test
    public void testRemoveSurroundingQuotesIfPresent() {
        assertEquals("Surrounded by quotes", removeSurroundingQuotesIfPresent("\"Surrounded by quotes\""));
        assertEquals("Not surrounded by quotes", removeSurroundingQuotesIfPresent("Not surrounded by quotes"));
        assertEquals("Contains \"quotes\"", removeSurroundingQuotesIfPresent("Contains \"quotes\""));
        assertEquals("\"Only starts with quotes", removeSurroundingQuotesIfPresent("\"Only starts with quotes"));
        assertEquals("Only ends with quotes\"", removeSurroundingQuotesIfPresent("Only ends with quotes\""));
    }
}
