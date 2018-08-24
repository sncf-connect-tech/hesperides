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

import org.hesperides.core.domain.templatecontainers.exceptions.RequiredPropertyCannotHaveDefaultValueException;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PropertyTest {

    @Test
    public void testExtractProperty() {

        //Cas nominaux

        assertProperty(new Property("named property", false, "", "", "", false),
                Property.extractProperty(" named property "));

        assertProperty(new Property("required property", true, null, "", "", false),
                Property.extractProperty("required property| @required "));

        assertProperty(new Property("comment without quotes", false, "comment", "", "", false),
                Property.extractProperty("comment without quotes| @comment comment"));

        assertProperty(new Property("comment with double quotes", false, "a comment", "", "", false),
                Property.extractProperty("comment with double quotes| @comment \"a comment\""));

        assertProperty(new Property("comment with simple quotes", false, "a comment", "", "", false),
                Property.extractProperty("comment with simple quotes| @comment 'a comment'"));

        assertProperty(new Property("default value", false, null, "12", "", false),
                Property.extractProperty("default value| @default 12"));

        assertProperty(new Property("with pattern", false, null, "", "*", false),
                Property.extractProperty(" with pattern | @pattern \"*\""));

        assertProperty(new Property("is password", false, null, "", "", true),
                Property.extractProperty(" is password | @password "));

        assertProperty(new Property("full without default value", true, "a comment", "", "*", true),
                Property.extractProperty(" full without default value | @required @comment 'a comment' @pattern * @password "));

        assertProperty(new Property("full not required", false, "a comment", "12", "*", true),
                Property.extractProperty(" full not required | @comment 'a comment' @default 12 @pattern * @password "));

        // Valeur d'annotation contenant un ou plusieurs pipes, typiquement un pattern

        assertProperty(new Property("pattern with pipe", false, null, "", "(a|b|c)", false),
                Property.extractProperty("pattern with pipe| @pattern (a|b|c)"));

        assertProperty(new Property("pattern with pipe", false, null, "", "(a|b|c)", false),
                Property.extractProperty("pattern with pipe| @pattern '(a|b|c)'"));

        // Annotations séparées par des pipes

        assertProperty(new Property("pipe separated annotations", false, "comment", "12", "", false),
                Property.extractProperty("pipe separated annotations | @comment comment | @default 12"));

        // Commentaire vide ou absent

        assertProperty(new Property("missing comment", false, null, "", "", false),
                Property.extractProperty("missing comment| @comment "));

        assertProperty(new Property("empty comment", false, null, "", "", false),
                Property.extractProperty("empty comment| @comment \"\""));

        assertProperty(new Property("empty comment", false, null, "", "", false),
                Property.extractProperty("empty comment| @comment ''"));

        assertProperty(new Property("blank comment", false, null, "", "", false),
                Property.extractProperty("blank comment|                   "));

        assertProperty(new Property("no comment", false, null, "", "", false),
                Property.extractProperty("no comment|"));

        // Commentaire avant annotation

        assertProperty(new Property("comment before annotation", true, "comment", "", "", false),
                Property.extractProperty("comment before annotation| comment @required "));

        assertProperty(new Property("comment with arobase before annotation", true, "@oops", "", "", false),
                Property.extractProperty("comment with arobase before annotation| @oops @required "));

        // Autres commentaires

        assertProperty(new Property("comment without annotation", false, "a comment", "", "", false),
                Property.extractProperty("comment without annotation | a comment "));

        assertProperty(new Property("comment without annotation but with double quotes", false, "\"a comment\"", "", "", false),
                Property.extractProperty("comment without annotation but with double quotes | \"a comment\""));

        assertProperty(new Property("comment without annotation but with simple quotes", false, "'a comment'", "", "", false),
                Property.extractProperty("comment without annotation but with simple quotes | 'a comment'"));

        assertProperty(new Property("comment without annotation but with arobase", false, "@admin and @user", "", "", false),
                Property.extractProperty("comment without annotation but with arobase| @admin and @user "));

        assertProperty(new Property("comment with annotation and arobase", false, "@admin and @user", "", "", false),
                Property.extractProperty("comment with annotation and arobase| @comment '@admin and @user'"));

        assertProperty(new Property("comment with annotation and arobase", false, "@admin", "", "", false),
                Property.extractProperty("comment with annotation and arobase| @comment @admin"));

        assertProperty(new Property("comment that is an email address", false, "name@email.com", "", "", false),
                Property.extractProperty("comment that is an email address| name@email.com "));

        assertProperty(new Property("comment that is an email address", false, "name@email.com", "", "", false),
                Property.extractProperty("comment that is an email address| @comment name@email.com "));

        assertProperty(new Property("comment that is an email address", false, "name@email.com", "", "", false),
                Property.extractProperty("comment that is an email address| @comment 'name@email.com' "));

        // Annotations avec et sans majuscules

        assertProperty(new Property("REQUIRED", true, null, "", "", false),
                Property.extractProperty("REQUIRED|@REQUIRED"));

        assertProperty(new Property("Required", true, null, "", "", false),
                Property.extractProperty("Required|@Required"));

        // Multiple required et password autorisés

        assertProperty(new Property("multiple required and password", true, null, "", "", true),
                Property.extractProperty("multiple required and password|@required @required @password @password"));

        // #307 Annotation collée au texte

        assertProperty(new Property("comment typo", true, null, "", "", true),
                Property.extractProperty("comment typo|@commentForgot space @required @password"));
    }

    @Test
    public void legacyBugCases() {

        // Pipe

//        assertProperty(new Property("weird pipe separated annotations", false, null, "", "", false),
//                Property.extractProperty("weird pipe separated annotations | @comment \"comment | @default 12"));

        // Arobase

//        assertProperty(new Property("http.proxy", false, "Format : [protocol://][user:password", "", "", false),
//                Property.extractProperty("http.proxy | Format : [protocol://][user:password@]proxyhost[:port]"));
//
//        assertProperty(new Property("Module.PAHDeliveryMode", false, "Etat par d?faut du module P", "", "", false),
//                Property.extractProperty("Module.PAHDeliveryMode|Etat par d?faut du module P@H [STARTED/STOPPED] "));

        // Antislashes

        assertProperty(new Property("antislash", false, "\"\\\\u\"", "", "", false),
                Property.extractProperty("antislash|\"\\\\u\""));

        assertProperty(new Property("antislash", false, "\\\\u", "", "", false),
                Property.extractProperty("antislash|\\\\u"));

        assertProperty(new Property("antislash", false, "\\u", "", "", false),
                Property.extractProperty("antislash|@comment \"\\\\u\""));

        assertProperty(new Property("antislash", false, "\\\\u", "", "", false),
                Property.extractProperty("antislash|@comment \\\\u"));

        assertProperty(new Property("antislash", false, "\"\\u\"", "", "", false),
                Property.extractProperty("antislash|\"\\u\""));

        assertProperty(new Property("antislash", false, "\\u", "", "", false),
                Property.extractProperty("antislash|\\u"));

        assertProperty(new Property("antislash", false, "u", "", "", false),
                Property.extractProperty("antislash|@comment \"\\u\""));

        assertProperty(new Property("antislash", false, "\\u", "", "", false),
                Property.extractProperty("antislash|@comment \\u"));

        assertProperty(new Property("antislash", false, "\"\\\\\\u\"", "", "", false),
                Property.extractProperty("antislash|\"\\\\\\u\""));

        assertProperty(new Property("antislash", false, "\\\\\\u", "", "", false),
                Property.extractProperty("antislash|\\\\\\u"));

        assertProperty(new Property("antislash", false, "\\u", "", "", false),
                Property.extractProperty("antislash|@comment \"\\\\\\u\""));

        assertProperty(new Property("antislash", false, "\\\\\\u", "", "", false),
                Property.extractProperty("antislash|@comment \\\\\\u"));

        assertProperty(new Property("antislash", false, "u", "", "", false),
                Property.extractProperty("antislash|@comment \"\\u\""));

        assertProperty(new Property("antislash", false, "\\u", "", "", false),
                Property.extractProperty("antislash|@comment \"\\\\u\""));

        assertProperty(new Property("antislash", false, "\\u", "", "", false),
                Property.extractProperty("antislash|@comment \"\\\\\\u\""));

        assertProperty(new Property("antislash", false, "\\\\u", "", "", false),
                Property.extractProperty("antislash|@comment \"\\\\\\\\u\""));

        assertProperty(new Property("antislash", false, "\\\\u", "", "", false),
                Property.extractProperty("antislash|@comment \"\\\\\\\\\\u\""));
    }

    @Test
    public void diff() {

        assertProperty(new Property("tn.customer.file.api.login", false, "Login pour l'authentification à l'api", "", "", false),
                Property.extractProperty("tn.customer.file.api.login| @comment 'Login pour l\\'authentification à l\\'api'"));

        assertProperty(new Property("pao.reference.data.cachemanager.name", false, "Nom du cache manager des donnees de reference", "pao-bridge-jms-reference-data-cache", "", false),
                Property.extractProperty("pao.reference.data.cachemanager.name|@comment \"Nom du cache manager des donnees de reference\"|@default pao-bridge-jms-reference-data-cache"));
    }

    private void assertProperty(Property expectedProperty, Property actualProperty) {
        assertEquals(expectedProperty.getName(), actualProperty.getName());
        assertEquals(expectedProperty.isRequired(), actualProperty.isRequired());
        assertEquals(expectedProperty.getComment(), actualProperty.getComment());
        assertEquals(expectedProperty.getDefaultValue(), actualProperty.getDefaultValue());
        assertEquals(expectedProperty.getPattern(), actualProperty.getPattern());
        assertEquals(expectedProperty.isPassword(), actualProperty.isPassword());
    }

    @Test(expected = IllegalArgumentException.class)
    public void requiredWithValueIsNotAllowed() {
        Property.extractProperty("required with value|@required true");
    }

    @Test(expected = IllegalArgumentException.class)
    public void passwordWithValueIsNotAllowed() {
        Property.extractProperty("password with value|@password true");
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingDefaultValueIsNotAllowed() {
        Property.extractProperty("missing default value|@default ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyDefaultValueIsNotAllowed() {
        Property.extractProperty("empty default value|@default \"\"");
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankDefaultValueIsNotAllowed() {
        Property.extractProperty("blank default value|@default \"  \"");
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingPatternIsNotAllowed() {
        Property.extractProperty("missing pattern|@pattern ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyPatternIsNotAllowed() {
        Property.extractProperty("empty pattern|@pattern \"\"");
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankPatternIsNotAllowed() {
        Property.extractProperty("blank pattern|@pattern \" \"");
    }

    @Test(expected = IllegalArgumentException.class)
    public void overriddenCommentIsNotAllowed() {
        Property.extractProperty("overridden comment|first comment @comment 'second one'");
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownAnnotationIsNotAllowed() {
        // Ce test est OK pour l'instant mais ne le sera plus quand il faudra utliser des exceptions spécifiques
        Property.extractProperty("unknown annotation|@default @oops");
    }

    @Test(expected = RequiredPropertyCannotHaveDefaultValueException.class)
    public void requiredDefaultValueIsNotAllowed() {
        Property.extractProperty("required default value|@required @default 12");
    }

    @Test(expected = IllegalArgumentException.class)
    public void multipleCommentsAreNotAllowed() {
        Property.extractProperty("multiple comments|@comment x @comment y");
    }

    @Test(expected = IllegalArgumentException.class)
    public void multipleDefaultsAreNotAllowed() {
        Property.extractProperty("multiple defaults|@default 12 @default 13");
    }

    @Test(expected = IllegalArgumentException.class)
    public void multiplePatternsAreNotAllowed() {
        Property.extractProperty("multiple patterns|@pattern * @pattern ?");
    }

    @Test(expected = IllegalArgumentException.class)
    public void patternThatStartsButDoesntEndWithQuotesIsNotAllowed() {
        Property.extractProperty("pattern that starts but doesn't end with quotes | @pattern \"*");
    }

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
        assertEquals("\" comment \"", Property.extractValueBeforeFirstKnownAnnotation(" \" comment \" @required"));
        assertEquals("' comment '", Property.extractValueBeforeFirstKnownAnnotation(" ' comment ' @required"));
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
    public void testExtractPropertiesFromStringContent() {
        List<AbstractProperty> properties = Property.extractPropertiesFromStringContent("{{ foo}} {{bar }} {{ fub }}");
        assertEquals(3, properties.size());
        assertEquals("foo", properties.get(0).getName());
        assertEquals("bar", properties.get(1).getName());
        assertEquals("fub", properties.get(2).getName());
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
