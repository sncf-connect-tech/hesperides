package org.hesperides.domain.templatecontainer.entities;

import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@NonFinal
public class Property {

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
