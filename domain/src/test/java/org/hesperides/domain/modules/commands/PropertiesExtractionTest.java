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

import org.hesperides.domain.templatecontainers.entities.AbstractProperty;
import org.hesperides.domain.templatecontainers.entities.IterableProperty;
import org.hesperides.domain.templatecontainers.entities.Property;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PropertiesExtractionTest {

    @Test
    public void testExtractPropertiesFromStringContent() {
        List<AbstractProperty> properties = Property.extractPropertiesFromStringContent("{{ foo}} {{bar }} {{ fub }}");
        assertEquals(3, properties.size());
        assertEquals("foo", properties.get(0).getName());
        assertEquals("bar", properties.get(1).getName());
        assertEquals("fub", properties.get(2).getName());
    }

    @Test
    public void testExtractPropertyFromStringDefinition() {
        Property requiredCommentPatternPassword = Property.extractPropertyFromStringDefinition("foo|@required @comment \"a comment\" @pattern \"a pattern\" @password");
        assertProperty(new Property("foo", true, "a comment", "", "a pattern", true), requiredCommentPatternPassword);

        Property commentDefaultPatternPassword = Property.extractPropertyFromStringDefinition("foo|@comment \"a comment\" @default 12 @pattern \"a pattern\" @password");
        assertProperty(new Property("foo", false, "a comment", "12", "a pattern", true), commentDefaultPatternPassword);

        Property noAnnotationProperty = Property.extractPropertyFromStringDefinition("foo");
        assertProperty(new Property("foo", false, "", "", "", false), noAnnotationProperty);

        Property requiredPropery = Property.extractPropertyFromStringDefinition("foo|@required");
        assertProperty(new Property("foo", true, "", "", "", false), requiredPropery);

        Property commentProperty = Property.extractPropertyFromStringDefinition("foo|@comment \"a comment\"");
        assertProperty(new Property("foo", false, "a comment", "", "", false), commentProperty);

        Property defaultProperty = Property.extractPropertyFromStringDefinition("foo|@default 5");
        assertProperty(new Property("foo", false, "", "5", "", false), defaultProperty);

        Property patternProperty = Property.extractPropertyFromStringDefinition("foo|@pattern \"z\"");
        assertProperty(new Property("foo", false, "", "", "z", false), patternProperty);

        Property passwordProperty = Property.extractPropertyFromStringDefinition("foo|@password");
        assertProperty(new Property("foo", false, "", "", "", true), passwordProperty);

        Property moreThanOneAnnotationProperty = Property.extractPropertyFromStringDefinition("foo|@required   @comment \"a comment\"");
        assertProperty(new Property("foo", true, "a comment", "", "", false), moreThanOneAnnotationProperty);

        //TODO Tester le fait qu'on ne peut pas utiliser @required @default dans la même propriété
    }

    private void assertProperty(Property expectedProperty, Property actualProperty) {
        assertEquals(expectedProperty.getName(), actualProperty.getName());
        assertEquals(expectedProperty.isRequired(), actualProperty.isRequired());
        assertEquals(expectedProperty.getComment(), actualProperty.getComment());
        assertEquals(expectedProperty.getDefaultValue(), actualProperty.getDefaultValue());
        assertEquals(expectedProperty.getPattern(), actualProperty.getPattern());
        assertEquals(expectedProperty.isPassword(), actualProperty.isPassword());
    }

    @Test
    public void testExtractPropertyAnnotationValue() {
        assertEquals("something without any quotes", Property.extractPropertyAnnotationValue("anyOption something without any quotes"));
        assertEquals("something with quotes", Property.extractPropertyAnnotationValue("anyOption \"something with quotes\" "));
        assertEquals("12", Property.extractPropertyAnnotationValue("anyOption 12"));
        assertEquals("something else", Property.extractPropertyAnnotationValue("anyOption   something else      "));
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
        String content = "{{#a}}{{foo|@required}}{{#b}}{{bar|@default zzz}}{{/b}}{{/a}}";
        List<AbstractProperty> abstractProperties = AbstractProperty.extractPropertiesFromStringContent(content);
        IterableProperty iterablePropertyA = (IterableProperty) abstractProperties.get(0);
        assertEquals("a", iterablePropertyA.getName());
        Property propertyFoo = (Property) iterablePropertyA.getProperties().get(0);
        assertProperty(new Property("foo", true, "", "", "", false), propertyFoo);
        IterableProperty iterablePropertyB = (IterableProperty) iterablePropertyA.getProperties().get(1);
        assertEquals("b", iterablePropertyB.getName());
        Property propertyBar = (Property) iterablePropertyB.getProperties().get(0);
        assertProperty(new Property("bar", false, "", "zzz", "", false), propertyBar);
    }
}
