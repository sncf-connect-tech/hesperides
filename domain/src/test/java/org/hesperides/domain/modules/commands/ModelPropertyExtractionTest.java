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
package org.hesperides.domain.modules.commands;

import org.hesperides.domain.templatecontainer.entities.Model;
import org.hesperides.domain.templatecontainer.entities.Property;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ModelPropertyExtractionTest {

    @Test
    public void testExtractPropertiesFromStringContent() {
        List<Property> properties = Model.extractPropertiesFromStringContent("{{ foo}} {{bar }} {{ fub }}");
        assertEquals(3, properties.size());
        assertEquals("foo", properties.get(0).getName());
        assertEquals("bar", properties.get(1).getName());
        assertEquals("fub", properties.get(2).getName());
    }

    @Test
    public void testExtractPropertyFromStringDefinition() {
        Property completeProperty = Property.extractPropertyFromStringDefinition("foo|@required|@comment \"comment\"|@default 5|@pattern \"pattern\"|@password");
        assertEquals("foo", completeProperty.getName());
        assertEquals(true, completeProperty.isRequired());
        assertEquals("comment", completeProperty.getComment());
        assertEquals("5", completeProperty.getDefaultValue());
        assertEquals("pattern", completeProperty.getPattern());
        assertEquals(true, completeProperty.isPassword());

        Property minimalistProperty = Property.extractPropertyFromStringDefinition("bar");
        assertEquals("bar", minimalistProperty.getName());
        assertEquals(false, minimalistProperty.isRequired());
        assertEquals("", minimalistProperty.getComment());
        assertEquals("", minimalistProperty.getDefaultValue());
        assertEquals("", minimalistProperty.getPattern());
        assertEquals(false, minimalistProperty.isPassword());
    }

    @Test
    public void testExtractPropertyAttributeValue() {
        assertEquals("something without any quotes", Property.extractPropertyAttributeValue("@anyOption something without any quotes"));
        assertEquals("something with quotes", Property.extractPropertyAttributeValue(" @anyOption \"something with quotes\" "));
        assertEquals("12", Property.extractPropertyAttributeValue("@anyOption 12"));
        assertEquals("something else", Property.extractPropertyAttributeValue("   something that should not be there   @anyOption   something else      "));
    }

    @Test
    public void testRemoveSurroundingQuotesIfPresent() {
        assertEquals("Surrounded by quotes", Property.removeSurroundingQuotesIfPresent("\"Surrounded by quotes\""));
        assertEquals("Not surrounded by quotes", Property.removeSurroundingQuotesIfPresent("Not surrounded by quotes"));
        assertEquals("Contains \"quotes\"", Property.removeSurroundingQuotesIfPresent("Contains \"quotes\""));
        assertEquals("\"Only starts with quotes", Property.removeSurroundingQuotesIfPresent("\"Only starts with quotes"));
        assertEquals("Only ends with quotes\"", Property.removeSurroundingQuotesIfPresent("Only ends with quotes\""));
    }

    @Test
    public void testExtractIterablePropertiesFromStringContent() {

    }
}
