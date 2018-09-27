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

import org.hesperides.core.domain.templatecontainers.exceptions.RequiredPropertyWithDefaultValueException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PropertyTest {

    private void assertProperty(Property expectedProperty, Property actualProperty) {
        assertEquals(expectedProperty.getName(), actualProperty.getName());
        assertEquals(expectedProperty.isRequired(), actualProperty.isRequired());
        assertEquals(expectedProperty.getComment(), actualProperty.getComment());
        assertEquals(expectedProperty.getDefaultValue(), actualProperty.getDefaultValue());
        assertEquals(expectedProperty.getPattern(), actualProperty.getPattern());
        assertEquals(expectedProperty.isPassword(), actualProperty.isPassword());
    }

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

        // Autres commentaires

        assertProperty(new Property("comment without annotation", false, "a comment", "", "", false),
                Property.extractProperty("comment without annotation | a comment "));

        assertProperty(new Property("comment without annotation but with double quotes", false, "\"a comment\"", "", "", false),
                Property.extractProperty("comment without annotation but with double quotes | \"a comment\""));

        assertProperty(new Property("comment without annotation but with simple quotes", false, "'a comment'", "", "", false),
                Property.extractProperty("comment without annotation but with simple quotes | 'a comment'"));

        assertProperty(new Property("comment with annotation and arobase", false, "@admin and @user", "", "", false),
                Property.extractProperty("comment with annotation and arobase| @comment '@admin and @user'"));

        assertProperty(new Property("comment with annotation and arobase", false, "@admin", "", "", false),
                Property.extractProperty("comment with annotation and arobase| @comment @admin"));

        assertProperty(new Property("comment that is an email address", false, "name", "", "", false),
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

        // #313
        assertProperty(new Property("quoted comment containing escaped quotes", false, "Login pour l'authentification à l'api", "", "", false),
                Property.extractProperty("quoted comment containing escaped quotes| @comment 'Login pour l\\'authentification à l\\'api'"));

        // #317
        assertProperty(new Property("quoted values that begins and ends with space", false, "a comment", " a default ", " a pattern ", false),
                Property.extractProperty("quoted values that begins and ends with space| @comment \" a comment \" @default \" a default \" @pattern \" a pattern \" "));

        // #320
        assertProperty(new Property("comment wrapped with simple quotes but containing double quotes", false, "exemple: \"Bonjour\"", "", "", false),
                Property.extractProperty("comment wrapped with simple quotes but containing double quotes|@comment 'exemple: \"Bonjour\"'"));

        assertProperty(new Property("comment wrapped with double quotes but containing simple quotes", false, "exemple: 'Bonjour'", "", "", false),
                Property.extractProperty("comment wrapped with double quotes but containing simple quotes|@comment \"exemple: 'Bonjour'\""));

        // #326
        assertProperty(new Property("prop", false, "Foo", "", "", false),
                Property.extractProperty("prop|@comment Foo\tbar"));

        // #327
        assertProperty(new Property("passengerType.companion.list", false, "Comma-separated list of passenger type (paxtype) to be considered as companion passengers.", "GG99AD", "", false),
                Property.extractProperty("passengerType.companion.list|@Default \"GG99AD\" @Comment \"Comma-separated list of passenger type (paxtype) to be considered as companion passengers.\""));
    }

    @Test
    public void oldCommentArobase() {

        // #312
        assertProperty(new Property("http.proxy", false, "Format : [protocol://][user:password", "", "", false),
                Property.extractProperty("http.proxy | Format : [protocol://][user:password@]proxyhost[:port]"));

        assertProperty(new Property("comment space arobase", false, "comment @arobase", "", "", false),
                Property.extractProperty("comment space arobase| comment @arobase"));

        assertProperty(new Property("comment arobase", false, "comment", "", "", false),
                Property.extractProperty("comment arobase| comment@arobase"));

        assertProperty(new Property("arobase space comment", false, "@arobase comment", "", "", false),
                Property.extractProperty("arobase space comment| @arobase comment"));

        assertProperty(new Property("arobase", false, "@arobase", "", "", false),
                Property.extractProperty("arobase| @arobase"));

        assertProperty(new Property("comment with arobase before annotation", true, null, "", "", false),
                Property.extractProperty("comment with arobase before annotation| @oops @required "));

        assertProperty(new Property("old comment that starts with arobase", false, "@Tag de pub DART pour le mail de conf", "", "", false),
                Property.extractProperty("old comment that starts with arobase|@Tag de pub DART pour le mail de conf"));

        assertProperty(new Property("atdesti.bookingManagement.url", false, "URL vers la page de gestion des commandes Open Jaw (dossier", "", "", false),
                Property.extractProperty("atdesti.bookingManagement.url|URL vers la page de gestion des commandes Open Jaw (dossier @desti)"));

        assertProperty(new Property("atdesti.bookingManagement.url", false, "URL vers la page de gestion des @aro commandes Open Jaw (dossier @desti)", "", "", false),
                Property.extractProperty("atdesti.bookingManagement.url|URL vers la page de gestion des @aro commandes Open Jaw (dossier @desti)"));

        assertProperty(new Property("rcad.email.terminaison.adresse", false, null, "", "", false),
                Property.extractProperty("rcad.email.terminaison.adresse|@contratpro.fr [chaine] "));

        assertProperty(new Property("mail.support.technique.incident", false, "surcharge des @ mails/Adresse mail ou sont diriges les mails concernant les incidents techniques sur le paiement", "", "", false),
                Property.extractProperty("mail.support.technique.incident|surcharge des @ mails/Adresse mail ou sont diriges les mails concernant les incidents techniques sur le paiement"));
    }

    @Test
    public void legacyBugCases() {

        // #307 Annotation collée au texte
        assertProperty(new Property("comment typo", true, null, "", "", true),
                Property.extractProperty("comment typo|@commentForgot space @required @password"));

        // #311
        assertProperty(new Property("comment that only starts with quotes", false, null, "", "", false),
                Property.extractProperty("comment that only starts with quotes | @comment \"comment | @default 12"));


        // #314
        assertProperty(new Property("annotation after unrequired pipe", false, "Niveau des logs du package org.springframework.web|", "", "", true),
                Property.extractProperty("annotation after unrequired pipe|Niveau des logs du package org.springframework.web|@default ERROR|@pattern * @password"));

        // #315
        assertProperty(new Property("sumo.rules.generation", false, "true ou false", "", "", false),
                Property.extractProperty("sumo.rules.generation|true ou false@required"));

        // #318
        assertProperty(new Property("mur.url", false, null, "", "", false),
                Property.extractProperty("mur.url|@required|url du service mur"));

        assertProperty(new Property("thalys.newsletter.password", true, null, "", "", false),
                Property.extractProperty("thalys.newsletter.password|@required @password|password pour le service de newsletter"));

        assertProperty(new Property("sidh.uri", false, "SIDH URL (http://uvsclbh01-vip05:50707/sidh1i/)", "", "", false),
                Property.extractProperty("sidh.uri|@comment \"SIDH URL (http://uvsclbh01-vip05:50707/sidh1i/)\" @required\""));

        // #321
        assertProperty(new Property("authentication.appKey", false, "Aujourd", "", "", false),
                Property.extractProperty("authentication.appKey | @comment 'Aujourd'hui'"));

        assertProperty(new Property("authentication.appKey", false, "Aujourd", "", "", false),
                Property.extractProperty("authentication.appKey | @comment \"Aujourd\"hui\""));

        // #323
        assertProperty(new Property("ector.service.environment", false, null, "prod|@comment", "", false),
                Property.extractProperty("ector.service.environment|@default prod|@comment \"Valorisation possible: prod ou validation\""));

        // #324
        assertProperty(new Property("sumon.graphite.enabled", false, "Activation de la publication des metrics SUMON dans graphite @default false", "", "", false),
                Property.extractProperty("sumon.graphite.enabled|@comment \"Activation de la publication des metrics SUMON dans graphite @default false\""));

        assertProperty(new Property("newrest.key_api_key", false, null, "X-APIKEY", "", false),
                Property.extractProperty("newrest.key_api_key | @default \"X-APIKEY\" @comment \"Key for send apiKey in header"));
    }

    @Test
    public void testArobaseEndsWithSpaceOrIsTheEnd() {
        assertEquals(true, Property.arobaseEndsWithSpaceOrIsTheEnd("@foo "));
        assertEquals(true, Property.arobaseEndsWithSpaceOrIsTheEnd("@foo"));
        assertEquals(false, Property.arobaseEndsWithSpaceOrIsTheEnd("@foo)"));
        assertEquals(false, Property.arobaseEndsWithSpaceOrIsTheEnd("@foo-"));
    }

    @Test
    public void legacyAntislashes() {

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

    @Test(expected = RequiredPropertyWithDefaultValueException.class)
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

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void commentWithoutAnnotationButWithTwoArobase() {
        Property.extractProperty("comment without annotation but with 2 arobases| @admin and @user");
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
        assertEquals("\" comment \"", Property.extractValueBeforeFirstKnownAnnotation(" \" comment \" @required"));
        assertEquals("' comment '", Property.extractValueBeforeFirstKnownAnnotation(" ' comment ' @required"));
        assertEquals("comment", Property.extractValueBeforeFirstKnownAnnotation(" comment "));
        assertEquals("comment @what", Property.extractValueBeforeFirstKnownAnnotation(" comment @what"));
    }

    @Test
    public void extractAnnotationValueLegacyStyle() {
        assertEquals("a", Property.extractAnnotationValueLegacyStyle(" @comment a comment "));
        assertEquals(" a comment ", Property.extractAnnotationValueLegacyStyle(" @comment \" a comment \" "));
        assertEquals(" a comment ", Property.extractAnnotationValueLegacyStyle(" @comment ' a comment ' "));
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
        assertEquals("Surrounded by simple quotes", Property.extractValueBetweenQuotes("'Surrounded by simple quotes'"));
//        assertEquals("Surrounded by simple quotes and containing 'escaped simple quotes', yes...", Property.extractValueBetweenQuotes("'Surrounded by simple quotes and containing \'escaped simple quotes\', yes...'"));
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
