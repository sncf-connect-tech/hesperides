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
package org.hesperides.core.domain.templatecontainers.entities;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PropertyTest {

    @Test
    public void testStartsWithKnownAnnotation() {
        assertEquals(true, Property.startsWithKnownAnnotation("@required"));
        assertEquals(true, Property.startsWithKnownAnnotation(" @comment x"));
        assertEquals(true, Property.startsWithKnownAnnotation("@default 12"));
        assertEquals(true, Property.startsWithKnownAnnotation(" @pattern z"));
        assertEquals(true, Property.startsWithKnownAnnotation("@password"));
        assertEquals(false, Property.startsWithKnownAnnotation("@what @required"));
        assertEquals(false, Property.startsWithKnownAnnotation(" zzz @required"));
    }

    @Test
    public void testExtractValueBeforeFirstKnownAnnotation() {
        assertEquals("anything", Property.extractValueBeforeFirstKnownAnnotation(" anything @required "));
        assertEquals("@what what", Property.extractValueBeforeFirstKnownAnnotation(" @what what @required "));
        assertEquals("\"comment\"", Property.extractValueBeforeFirstKnownAnnotation("\"comment\" @required"));
        assertEquals("comment", Property.extractValueBeforeFirstKnownAnnotation(" comment "));
        assertEquals("comment @what", Property.extractValueBeforeFirstKnownAnnotation(" comment @what"));
        assertEquals("@what comment", Property.extractValueBeforeFirstKnownAnnotation("@what comment "));
    }

    @Test
    public void extractAnnotationValueLegacyStyle() {
        assertEquals("a", Property.extractAnnotationValueLegacyStyle(" @comment a comment "));
        assertEquals("a comment", Property.extractAnnotationValueLegacyStyle(" @comment \" a comment \" "));
        assertEquals("a comment", Property.extractAnnotationValueLegacyStyle(" @comment ' a comment ' "));
        assertEquals(null, Property.extractAnnotationValueLegacyStyle(" @comment \" a comment "));
        assertEquals(null, Property.extractAnnotationValueLegacyStyle(" @comment ' a comment "));
        assertEquals("ab", Property.extractAnnotationValueLegacyStyle(" @comment \"ab\"cd "));
        assertEquals("ab\"cd", Property.extractAnnotationValueLegacyStyle(" @comment ab\"cd "));
        assertEquals("ab\"cd\"", Property.extractAnnotationValueLegacyStyle(" @comment ab\"cd\" ef "));
        assertEquals("ab\"cd\"", Property.extractAnnotationValueLegacyStyle(" @comment ab\"cd\" \"ef "));
    }

    @Test
    public void test() {
        assertProperty(
                new Property("foo", false, "\"comment without annotation but with double quotes\"", "", "", false),
                Property.extractPropertyFromStringDefinition("foo | \"comment without annotation but with double quotes\"")
        );
        assertProperty(
                new Property("foo", false, "'content with simple quotes'", "", "", false),
                Property.extractPropertyFromStringDefinition("foo | 'content with simple quotes'")
        );
        assertProperty(
                new Property("foo", false, "content", "", "", false),
                Property.extractPropertyFromStringDefinition("foo | content")
        );
        assertProperty(
                new Property("foo", false, "content", "", "", false),
                Property.extractPropertyFromStringDefinition("foo|content")
        );
        assertProperty(
                new Property("foo", false, "content", "12", "*", true),
                Property.extractPropertyFromStringDefinition("foo | @comment content of template-a @default 12 @pattern * @password")
        );
        assertProperty(
                new Property("var", false, "var is commented and can be composed with spaces", "", "", false),
                Property.extractPropertyFromStringDefinition("var|var is commented and can be composed with spaces")
        );
        assertProperty(
                new Property("var", false, "var is commented and has a", "default_value", "", false),
                Property.extractPropertyFromStringDefinition("var|var is commented and has a @default default_value")
        );
        assertProperty(
                new Property("var", false, null, "comment", "", false),
                Property.extractPropertyFromStringDefinition("var|@default comment comment Should Be First")
        );
        assertProperty(
                new Property("var", false, "cantHaveSpace", "okayValue", "", false),
                Property.extractPropertyFromStringDefinition("var|@default okayValue @comment cantHaveSpace orElse")
        );
        assertProperty(
                new Property("var", false, "spaced", "", "", false),
                Property.extractPropertyFromStringDefinition("var|@comment spaced comment")
        );
        assertProperty(
                new Property("var", false, "super long comment", "super", "", false),
                Property.extractPropertyFromStringDefinition("var|@comment \"super long comment\" @default super long default")
        );
        assertProperty(
                new Property("var", false, "\"comment between quotes\"", "", "", false),
                Property.extractPropertyFromStringDefinition("var|\"comment between quotes\"")
        );
        assertProperty(
                new Property("var", false, null, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean a mauris auctor nunc ultricies aliquam et ac ipsum. Curabitur lectus urna, accumsan eget sapien et, lacinia lobortis sapien.", "", false),
                Property.extractPropertyFromStringDefinition("var|@default \"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean a mauris auctor nunc ultricies aliquam et ac ipsum. Curabitur lectus urna, accumsan eget sapien et, lacinia lobortis sapien.\"")
        );
        assertProperty(
                new Property("var", false, null, "bob@bob.com", "", false),
                Property.extractPropertyFromStringDefinition("var|@Default \"bob@bob.com\"")
        );
        assertProperty(
                new Property("var", false, null, "bob@bob.com", "", false),
                Property.extractPropertyFromStringDefinition("var|@Default bob@bob.com")
        );
    }

    @Test
    public void testExtractAnnotationValueLegacyStyle() {
        assertEquals("something", Property.extractAnnotationValueLegacyStyle("@annotation something in the way"));
        assertEquals("something in the way", Property.extractAnnotationValueLegacyStyle("@annotation \"something in the way\""));
        assertEquals("something in", Property.extractAnnotationValueLegacyStyle("@annotation \"something in\" the way\""));
        assertEquals(null, Property.extractAnnotationValueLegacyStyle("@annotation \"something in the way"));
        assertEquals("some\"thing", Property.extractAnnotationValueLegacyStyle("@annotation some\"thing"));
    }

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
        Property patternWithPipes = Property.extractPropertyFromStringDefinition("foo|@required @pattern a|b|c");
        assertProperty(new Property("foo", true, null, "", "a|b|c", false), patternWithPipes);

        Property requiredCommentPatternPassword = Property.extractPropertyFromStringDefinition("foo|@required @comment \"a comment\" @pattern \"a pattern\" @password");
        assertProperty(new Property("foo", true, "a comment", "", "a pattern", true), requiredCommentPatternPassword);

        Property commentDefaultPatternPassword = Property.extractPropertyFromStringDefinition("foo|@comment \"a comment\" @default 12 @pattern \"a pattern\" @password");
        assertProperty(new Property("foo", false, "a comment", "12", "a pattern", true), commentDefaultPatternPassword);

        Property noAnnotationProperty = Property.extractPropertyFromStringDefinition("foo");
        assertProperty(new Property("foo", false, "", "", "", false), noAnnotationProperty);

        Property requiredPropery = Property.extractPropertyFromStringDefinition("foo|@required");
        assertProperty(new Property("foo", true, null, "", "", false), requiredPropery);

        Property commentProperty = Property.extractPropertyFromStringDefinition("foo|@comment \"a comment\"");
        assertProperty(new Property("foo", false, "a comment", "", "", false), commentProperty);

        Property defaultProperty = Property.extractPropertyFromStringDefinition("foo|@default 5");
        assertProperty(new Property("foo", false, null, "5", "", false), defaultProperty);

        Property patternProperty = Property.extractPropertyFromStringDefinition("foo|@pattern \"z\"");
        assertProperty(new Property("foo", false, null, "", "z", false), patternProperty);

        Property passwordProperty = Property.extractPropertyFromStringDefinition("foo|@password");
        assertProperty(new Property("foo", false, null, "", "", true), passwordProperty);

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
        assertEquals("something", Property.extractAnnotationValueLegacyStyle("anyOption something without any quotes"));
        assertEquals("something with quotes", Property.extractAnnotationValueLegacyStyle("anyOption \"something with quotes\" "));
        assertEquals("12", Property.extractAnnotationValueLegacyStyle("anyOption 12"));
        assertEquals("something", Property.extractAnnotationValueLegacyStyle("anyOption   something else      "));
    }

    @Test
    public void testExtractValueBetweenQuotes() {
        assertEquals("Surrounded by double quotes", Property.extractValueBetweenQuotes("\"Surrounded by double quotes\""));
        assertEquals("Surrounded by simple quotes", Property.extractValueBetweenQuotes("\"Surrounded by simple quotes\""));
        assertEquals(null, Property.extractValueBetweenQuotes("Not surrounded by simple quotes"));
    }

    @Test
    public void testExtractIterablePropertiesFromStringContent() {
        String content = "{{#a}}{{foo|@required}}{{#b}}{{bar|@default zzz}}{{/b}}{{/a}}";
        List<AbstractProperty> abstractProperties = AbstractProperty.extractPropertiesFromStringContent(content);
        IterableProperty iterablePropertyA = (IterableProperty) abstractProperties.get(0);
        assertEquals("a", iterablePropertyA.getName());
        Property propertyFoo = (Property) iterablePropertyA.getProperties().get(0);
        assertProperty(new Property("foo", true, null, "", "", false), propertyFoo);
        IterableProperty iterablePropertyB = (IterableProperty) iterablePropertyA.getProperties().get(1);
        assertEquals("b", iterablePropertyB.getName());
        Property propertyBar = (Property) iterablePropertyB.getProperties().get(0);
        assertProperty(new Property("bar", false, null, "zzz", "", false), propertyBar);
    }
}
