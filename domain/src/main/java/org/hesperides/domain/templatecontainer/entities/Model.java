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
package org.hesperides.domain.templatecontainer.entities;

import com.github.mustachejava.Code;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.codes.ValueCode;
import lombok.Value;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
public class Model {
    List<Property> properties;
    List<IterableProperty> iterableProperties;

    private static final String VARIABLE_ATTRIBUTES_SEPARATOR_REGEX = "[|]";
    private static final int VARIABLE_NAME_INDEX = 0;

    public static List<Property> extractProperties(String content) {
        List<Property> properties = new ArrayList<>();

        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mustacheFactory.compile(new StringReader(content), "something");
        for (Code code : mustache.getCodes()) {
            if (code instanceof ValueCode) {
                Property property = extractProperty(code.getName());
                if (property != null) {
                    properties.add(property);
                }
            }
        }
        return properties;
    }

    private static Property extractProperty(String propertyDefinition) {
        Property property = null;
        if (propertyDefinition != null) {
            String[] propertyAttributes = propertyDefinition.split(VARIABLE_ATTRIBUTES_SEPARATOR_REGEX);

            String name = propertyAttributes[VARIABLE_NAME_INDEX].trim();
            boolean isRequired = false;
            String comment = "";
            String defaultValue = "";
            String pattern = "";
            boolean isPassword = false;

            if (propertyAttributes.length > 1) {
                for (int i = VARIABLE_NAME_INDEX + 1; i < propertyAttributes.length; i++) {
                    String propertyAttribute = propertyAttributes[i];
                    Property.Option propertyOption = extractPropertyOption(propertyAttribute);
                    if (propertyOption != null) {
                        switch (propertyOption) {
                            case IS_REQUIRED:
                                isRequired = true;
                                break;
                            case COMMENT:
                                comment = extractPropertyOptionValue(propertyAttribute);
                                break;
                            case DEFAULT_VALUE:
                                defaultValue = extractPropertyOptionValue(propertyAttribute);
                                break;
                            case PATTERN:
                                pattern = extractPropertyOptionValue(propertyAttribute);
                                break;
                            case IS_PASSWORD:
                                isPassword = true;
                                break;
                        }
                    }
                }
            }
            property = new Property(name, isRequired, comment, defaultValue, pattern, isPassword);
        }
        return property;
    }

    /**
     * Extrait l'option détectée à l'aide d'une expression régulière qui ressemble à ceci : @(required|comment|default|pattern|password)
     */
    private static Property.Option extractPropertyOption(String value) {
        Property.Option option = null;
        if (value != null) {
            // Concatène les options avec "|" comme séparateur
            String options = Stream.of(Property.Option.values()).map(Property.Option::getName).collect(Collectors.joining("|"));
            Pattern pattern = Pattern.compile("@(" + options + ")", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                String optionName = matcher.group(1);
                option = Property.Option.fromName(optionName);
            }
        }
        return option;
    }

    /**
     * Détecte le premier espace (' ') après le premier @
     */
    private static String extractPropertyOptionValue(String variableOption) {
        int indexOfFirstArobase = variableOption.indexOf("@");
        String valueStartingAtFirstArobase = variableOption.substring(indexOfFirstArobase);
        int indexOfFirstSpaceAfterOption = valueStartingAtFirstArobase.indexOf(" ");
        String valueThatMayBeSurroundedByQuotes = valueStartingAtFirstArobase.substring(indexOfFirstSpaceAfterOption);
        return removeSurroundingQuotesIfPresent(valueThatMayBeSurroundedByQuotes.trim());
    }

    private static String removeSurroundingQuotesIfPresent(String value) {
        boolean startsWithQuotes = "\"".equals(value.substring(0, 1));
        boolean endsWithQuotes = "\"".equals(value.substring(value.length() - 1));
        if (startsWithQuotes && endsWithQuotes) {
            value = value.substring(1, value.length() - 1);
        }
        return value.trim();
    }
}
