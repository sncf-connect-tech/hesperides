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
import org.hesperides.domain.templatecontainer.queries.PropertiesModel;
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

    enum VariableOption {
        IS_REQUIRED("required"),
        COMMENT("comment"),
        DEFAULT_VALUE("default"),
        PATTERN("pattern"),
        IS_PASSWORD("password");

        private final String name;

        VariableOption(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static VariableOption fromName(String name) {
            VariableOption result = null;
            for (VariableOption option : VariableOption.values()) {
                if (option.getName().equalsIgnoreCase(name)) {
                    result = option;
                    break;
                }
            }
            return result;
        }
    }

    @Test
    public void testExtractKeyValueProperties() {
        List<PropertiesModel.KeyValueProperty> keyValueProperties = extractKeyValueProperties("{{ foo}} {{bar }} {{ fub }}");
        assertEquals(3, keyValueProperties.size());
        assertEquals("foo", keyValueProperties.get(0).getName());
        assertEquals("bar", keyValueProperties.get(1).getName());
        assertEquals("fub", keyValueProperties.get(2).getName());
    }

    public List<PropertiesModel.KeyValueProperty> extractKeyValueProperties(String content) {
        List<PropertiesModel.KeyValueProperty> keyValueProperties = new ArrayList<>();

        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mustacheFactory.compile(new StringReader(content), "something");
        for (Code code : mustache.getCodes()) {
            if (code instanceof ValueCode) {

                PropertiesModel.KeyValueProperty keyValueProperty = extractKeyValueProperty(code.getName());
                if (keyValueProperty != null) {
                    keyValueProperties.add(keyValueProperty);
                }
            }
        }
        return keyValueProperties;
    }

    @Test
    public void testExtractKeyValueProperty() {
        PropertiesModel.KeyValueProperty completeKeyValueProperty = extractKeyValueProperty("foo|@required|@comment \"comment\"|@default 5|@pattern \"pattern\"|@password");
        assertEquals("foo", completeKeyValueProperty.getName());
        assertEquals(true, completeKeyValueProperty.isRequired());
        assertEquals("comment", completeKeyValueProperty.getComment());
        assertEquals("5", completeKeyValueProperty.getDefaultValue());
        assertEquals("pattern", completeKeyValueProperty.getPattern());
        assertEquals(true, completeKeyValueProperty.isPassword());
        
        PropertiesModel.KeyValueProperty minimalistKeyValueProperty = extractKeyValueProperty("bar");
        assertEquals("bar", minimalistKeyValueProperty.getName());
        assertEquals(false, minimalistKeyValueProperty.isRequired());
        assertEquals("", minimalistKeyValueProperty.getComment());
        assertEquals("", minimalistKeyValueProperty.getDefaultValue());
        assertEquals("", minimalistKeyValueProperty.getPattern());
        assertEquals(false, minimalistKeyValueProperty.isPassword());
    }

    private PropertiesModel.KeyValueProperty extractKeyValueProperty(String variableDefinition) {
        PropertiesModel.KeyValueProperty keyValueProperty = null;
        if (variableDefinition != null) {
            String[] variableProperties = variableDefinition.split("[|]");

            String variableName = variableProperties[0].trim();
            boolean isRequired = false;
            String comment = "";
            String defaultValue = "";
            String pattern = "";
            boolean isPassword = false;

            if (variableProperties.length > 1) {
                for (int i = 1; i < variableProperties.length; i++) {
                    String variableProperty = variableProperties[i];
                    VariableOption variableOption = extractVariableOptionFrom(variableProperty);
                    if (variableOption != null) {
                        switch (variableOption) {
                            case IS_REQUIRED:
                                isRequired = true;
                                break;
                            case COMMENT:
                                comment = extractVariableOptionValue(variableProperty);
                                break;
                            case DEFAULT_VALUE:
                                defaultValue = extractVariableOptionValue(variableProperty);
                                break;
                            case PATTERN:
                                pattern = extractVariableOptionValue(variableProperty);
                                break;
                            case IS_PASSWORD:
                                isPassword = true;
                                break;
                        }
                    }
                }
            }
            keyValueProperty = new PropertiesModel.KeyValueProperty(variableName, isRequired, comment, defaultValue, pattern, isPassword);
        }
        return keyValueProperty;
    }

    /**
     * Extrait l'option détectée à l'aide d'une expression régulière qui ressemble à ceci : @(required|comment|default|pattern|password)
     */
    private VariableOption extractVariableOptionFrom(String value) {
        VariableOption variableOption = null;
        if (value != null) {
            // Concatène les options avec "|" comme séparateur
            String options = Stream.of(VariableOption.values()).map(VariableOption::getName).collect(Collectors.joining("|"));
            Pattern pattern = Pattern.compile("@(" + options + ")", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                String optionName = matcher.group(1);
                variableOption = VariableOption.fromName(optionName);
            }
        }
        return variableOption;
    }

    /**
     * Détecte le premier espace (' ') après le premier @
     */
    private String extractVariableOptionValue(String variableOption) {
        int indexOfFirstArobase = variableOption.indexOf("@");
        String valueStartingAtFirstArobase = variableOption.substring(indexOfFirstArobase);
        int indexOfFirstSpaceAfterOption = valueStartingAtFirstArobase.indexOf(" ");
        String valueThatMayBeSurroundedByQuotes = valueStartingAtFirstArobase.substring(indexOfFirstSpaceAfterOption);
        return removeSurroundingQuotesIfPresent(valueThatMayBeSurroundedByQuotes.trim());
    }

    private String removeSurroundingQuotesIfPresent(String value) {
        boolean startsWithQuotes = "\"".equals(value.substring(0, 1));
        boolean endsWithQuotes = "\"".equals(value.substring(value.length() - 1));
        if (startsWithQuotes && endsWithQuotes) {
            value = value.substring(1, value.length() - 1);
        }
        return value.trim();
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
