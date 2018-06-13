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
import com.github.mustachejava.codes.IterableCode;
import com.github.mustachejava.codes.ValueCode;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
public class Model {

    List<Property> properties;
    List<IterableProperty> iterableProperties;

    public static Model generateModelFromTemplates(Collection<Template> templates) {
        List<Model.Property> properties = new ArrayList<>();
        List<Model.IterableProperty> iterableProperties = new ArrayList<>();

        templates.forEach((template) -> {
            properties.addAll(extractPropertiesFromStringContent(template.getFilename()));
            properties.addAll(extractPropertiesFromStringContent(template.getLocation()));
            properties.addAll(extractPropertiesFromStringContent(template.getContent()));
            iterableProperties.addAll(extractIterablePropertiesFromStringContent(template.getFilename()));
            iterableProperties.addAll(extractIterablePropertiesFromStringContent(template.getLocation()));
            iterableProperties.addAll(extractIterablePropertiesFromStringContent(template.getContent()));
        });

        return new Model(properties, iterableProperties);
    }

    public static List<Property> extractPropertiesFromStringContent(String content) {
        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mustacheFactory.compile(new StringReader(content), "something");
        return extractPropertiesFromMustacheCodes(mustache.getCodes());
    }

    private static List<Property> extractPropertiesFromMustacheCodes(Code[] codes) {
        List<Property> properties = new ArrayList<>();
        for (Code code : codes) {
            if (code instanceof ValueCode) {
                Property property = Property.extractProperty(code.getName());
                if (property != null) {
                    properties.add(property);
                }
            }
        }
        return properties;
    }

    public static List<IterableProperty> extractIterablePropertiesFromStringContent(String content) {
        List<IterableProperty> iterableProperties = new ArrayList<>();

        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mustacheFactory.compile(new StringReader(content), "something");
        for (Code code : mustache.getCodes()) {
            if (code instanceof IterableCode) {
                Property parentProperty = Property.extractProperty(code.getName());
                if (parentProperty != null) {
                    List<Property> childProperties = extractPropertiesFromMustacheCodes(code.getCodes());
                    IterableProperty iterableProperty = new IterableProperty(
                            parentProperty.getName(),
                            parentProperty.isRequired(),
                            parentProperty.getComment(),
                            parentProperty.getDefaultValue(),
                            parentProperty.getPattern(),
                            parentProperty.isPassword(),
                            childProperties
                    );
                    iterableProperties.add(iterableProperty);
                }
            }
        }
        return iterableProperties;
    }

    @Value
    @NonFinal
    public static class Property {

        String name;
        boolean isRequired;
        String comment;
        String defaultValue;
        String pattern;
        boolean isPassword;

        public enum Attribute {
            IS_REQUIRED("required"),
            COMMENT("comment"),
            DEFAULT_VALUE("default"),
            PATTERN("pattern"),
            IS_PASSWORD("password");

            private final String name;

            Attribute(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            public static Attribute fromName(String name) {
                Attribute result = null;
                for (Attribute attribute : Attribute.values()) {
                    if (attribute.getName().equalsIgnoreCase(name)) {
                        result = attribute;
                        break;
                    }
                }
                return result;
            }
        }

        private static final String VARIABLE_ATTRIBUTES_SEPARATOR_REGEX = "[|]";
        private static final int VARIABLE_NAME_INDEX = 0;

        public static Property extractProperty(String propertyDefinition) {
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
                        String propertyAttributeDefinition = propertyAttributes[i];
                        Attribute propertyAttribute = extractPropertyAttribute(propertyAttributeDefinition);
                        if (propertyAttribute != null) {
                            switch (propertyAttribute) {
                                case IS_REQUIRED:
                                    isRequired = true;
                                    break;
                                case COMMENT:
                                    comment = extractPropertyAttributeValue(propertyAttributeDefinition);
                                    break;
                                case DEFAULT_VALUE:
                                    defaultValue = extractPropertyAttributeValue(propertyAttributeDefinition);
                                    break;
                                case PATTERN:
                                    pattern = extractPropertyAttributeValue(propertyAttributeDefinition);
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
         * TODO tester unitairement
         */
        private static Attribute extractPropertyAttribute(String value) {
            Attribute attribute = null;
            if (value != null) {
                // Concatène les options avec "|" comme séparateur
                String options = Stream.of(Attribute.values()).map(Attribute::getName).collect(Collectors.joining("|"));
                Pattern pattern = Pattern.compile("@(" + options + ")", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(value);
                if (matcher.find()) {
                    String optionName = matcher.group(1);
                    attribute = Attribute.fromName(optionName);
                }
            }
            return attribute;
        }

        /**
         * Détecte le premier espace (' ') après le premier @
         */
        public static String extractPropertyAttributeValue(String attribute) {
            int indexOfFirstArobase = attribute.indexOf("@");
            String valueStartingAtFirstArobase = attribute.substring(indexOfFirstArobase);
            int indexOfFirstSpaceAfterOption = valueStartingAtFirstArobase.indexOf(" ");
            String valueThatMayBeSurroundedByQuotes = valueStartingAtFirstArobase.substring(indexOfFirstSpaceAfterOption);
            return removeSurroundingQuotesIfPresent(valueThatMayBeSurroundedByQuotes.trim());
        }

        public static String removeSurroundingQuotesIfPresent(String value) {
            boolean startsWithQuotes = "\"".equals(value.substring(0, 1));
            boolean endsWithQuotes = "\"".equals(value.substring(value.length() - 1));
            if (startsWithQuotes && endsWithQuotes) {
                value = value.substring(1, value.length() - 1);
            }
            return value.trim();
        }
    }

    @Value
    public static class IterableProperty extends Property {

        List<Property> properties;

        public IterableProperty(String name, boolean isRequired, String comment, String defaultValue, String pattern, boolean isPassword, List<Property> properties) {
            super(name, isRequired, comment, defaultValue, pattern, isPassword);
            this.properties = properties;
        }
    }
}
