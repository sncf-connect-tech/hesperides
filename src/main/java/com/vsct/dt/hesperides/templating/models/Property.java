/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides.templating.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.mustachejava.codes.DefaultCode;
import com.vsct.dt.hesperides.templating.models.annotation.HesperidesAnnotation;
import com.vsct.dt.hesperides.templating.models.annotation.HesperidesAnnotationConstructor;
import com.vsct.dt.hesperides.templating.models.annotation.HesperidesCommentAnnotation;
import com.vsct.dt.hesperides.templating.models.exception.ModelAnnotationException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by william_montaz on 11/07/14.
 * Updated by Tidiane SIDIBE on 09/11/2016 : Add whitespaces ignoring on properties name
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"name", "comment", "required", "defaultValue", "pattern"})
public class Property {
    private final String name;
    private boolean password = false;
    private String comment = "";
    private boolean required = false;
    private String defaultValue = "";
    private String pattern = "";

    private static final Logger LOGGER = LoggerFactory.getLogger(Property.class);

    /**
     * Constructor from mustache codes
     * @param code : mustache code
     */
    public Property(final DefaultCode code) {
        String[] fields = extractFields(code);

        // We trim this for letting hesperides ignore the whitespaces on properties in the templates
        // Ex. {{ db.user.login  }} should be treated as {{db.user.login}}
        // This has impact to valorisation's name and mustache templates
        this.name = fields[0].trim();

        if (fields.length == 1) {
            return;
        }

        List<HesperidesAnnotation> annotationList = extractAnnotations(fields[1], this.name);
        for (HesperidesAnnotation annotation : annotationList) {
            switch (annotation.getName()) {
                case "comment" :
                    if (!this.comment.equals("")) {
                        throwDuplicateAnnotation("comment");
                    }
                    if (StringUtils.isBlank(annotation.getValue())) {
                        throwEmptyNotAllowed("comment");
                    }
                    this.comment = annotation.getValue();
                    break;
                case "default" :
                    if (this.required) {
                        throwBothRequiredAndDefault();
                    }
                    if (!this.defaultValue.equals("")) {
                        throwDuplicateAnnotation("default");
                    }
                    if (StringUtils.isBlank(annotation.getValue())) {
                        throwEmptyNotAllowed("default");
                    }
                    this.defaultValue = annotation.getValue();
                    break;
                case "pattern" :
                    if (!this.pattern.equals("")) {
                        throwDuplicateAnnotation("pattern");
                    }
                    if (StringUtils.isBlank(annotation.getValue())) {
                        throwEmptyNotAllowed("pattern");
                    }
                    this.pattern = annotation.getValue();
                    break;
                case "required" :
                    if (!this.defaultValue.equals("")) {
                        throwBothRequiredAndDefault();
                    }
                    if (this.required) {
                        throwDuplicateAnnotation("required");
                    }
                    this.required = true;
                    break;
                case "password" :
                    if (this.password) {
                        throwDuplicateAnnotation("password");
                    }
                    this.password = true;
                    break;
                default:
                    throw new ModelAnnotationException(
                            String.format("Annotation '%s' is not managed by property but Hesperides know it !",
                                    annotation.getName()));
            }
        }
    }

    private String[] extractFields(final DefaultCode code) {
        try {
            Field f = DefaultCode.class.getDeclaredField("name");
            f.setAccessible(true);
            String nameAndCommentString = (String) f.get(code);
            return nameAndCommentString.split("[|]", 2);
        } catch (final IllegalAccessException | NoSuchFieldException e) {
            LOGGER.debug(e.toString());
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructor from name and comment
     * Ex. {{hello|I am the comment}}
     *
     * @param name : the name
     * @param comment : the comment
     */
    @JsonCreator
    public Property(@JsonProperty("name") final String name,
                    @JsonProperty("comment") final String comment) {

        // We trim this for letting hesperides ignore the whitespaces on properties in the templates
        // Ex. {{ db.user.login  }} should be treated as {{db.user.login}}
        // This has impact to valorsiation's name and mustach templates
        this.name = name.trim();
        this.comment = comment;
    }

    private void throwBothRequiredAndDefault() {
        throw new ModelAnnotationException(
                String.format("Property '%s' canno't be @required and @default", this.name));
    }

    private void throwDuplicateAnnotation(final String annotationName) {
        throw new ModelAnnotationException(String.format("Duplicate annotations '@%s' found for property '%s'",
                annotationName, this.name));
    }

    private void throwEmptyNotAllowed(final String annotationName) {
        throw new ModelAnnotationException(String.format("Annotation '@%s' for property '%s' cannot be empty",
                annotationName, this.name));
    }

    /**
     * Extract annotations.
     *
     * @param fieldContent string after name of property
     * @param propertyName property name
     *
     * @return list of annotation
     */
    private static List<HesperidesAnnotation> extractAnnotations(String fieldContent, final String propertyName) {
        final List<HesperidesAnnotation> extractedAnnotations = new ArrayList<>();

        fieldContent = extractOldStyleComments(extractedAnnotations, fieldContent, propertyName);

        final int len = fieldContent.length();
        int currentWordStartingPos = -1;

        for (int index = 0; index < len; index++) {
            final char currentChar = fieldContent.charAt(index);
            if (Character.isWhitespace(currentChar)) {
                if (currentWordStartingPos != -1) {
                    index += addAnnotation(extractedAnnotations, fieldContent.substring(currentWordStartingPos, index), fieldContent.substring(index, len), propertyName);
                    currentWordStartingPos = -1;
                }
            } else if (currentWordStartingPos == -1) {
                currentWordStartingPos = index;
            }
        }
        if (currentWordStartingPos != -1) {
            addAnnotation(extractedAnnotations, fieldContent.substring(currentWordStartingPos, len), "", propertyName);
        }

        if (extractedAnnotations.isEmpty()) {
            throw new ModelAnnotationException(
                    String.format("Property '%s' has a second field without any annotation", propertyName));
        }

        return extractedAnnotations;
    }

    private static String extractOldStyleComments(final List<HesperidesAnnotation> extractedAnnotations, final String fieldContent, final String propertyName) {
        final int len = fieldContent.length();
        int index = 0;
        while (index < len && Character.isWhitespace(fieldContent.charAt(index))) {
            index++;
        }
        if (index < len && fieldContent.charAt(index) != '@') {
            HesperidesCommentAnnotation commentAnnotation = new HesperidesCommentAnnotation();
            int currentChar = fieldContent.charAt(index);
            final int firstNonBlankCharIndex = index;
            index++;
            int lastNonBlankCharIndex = index;
            while (index < len) {
                final char nextChar = fieldContent.charAt(index);
                if (Character.isWhitespace(currentChar) && nextChar == '@') { // stop comment when annotation start, but handle emails cases: toto@gouv.fr
                    break;
                }
                currentChar = nextChar;
                index++;
                if (!Character.isWhitespace(currentChar)) {
                    lastNonBlankCharIndex = index;
                }
            }
            commentAnnotation.setValue(fieldContent.substring(firstNonBlankCharIndex, lastNonBlankCharIndex));
            extractedAnnotations.add(commentAnnotation);
        }
        return fieldContent.substring(index, len);
    }

    private static int addAnnotation(final List<HesperidesAnnotation> extractedAnnotations, String annotationName, final String remainingFieldContent, final String propertyName) {
        if (annotationName.charAt(0) != '@') {
            throw new ModelAnnotationException(
                    String.format("Invalid annotation: expected character '@' but found '%s' for property '%s'", annotationName, propertyName));
        }
        annotationName = annotationName.substring(1, annotationName.length());
        int offset = 0;
        HesperidesAnnotation newAnnotation = HesperidesAnnotationConstructor.createAnnotationObject(annotationName, propertyName);
        if (newAnnotation.requireValue()) {
            AnnotationValue annotationValue = extractValueFromString(remainingFieldContent, annotationName, propertyName);
            offset += annotationValue.endPosition;
            newAnnotation.setValue(annotationValue.content);
        }
        extractedAnnotations.add(newAnnotation);
        return offset;
    }

    private static AnnotationValue extractValueFromString(String remainingFieldContent, final String annotationName, final String propertyName) {
        int firstNonBlankCharIndex = 0;
        while (firstNonBlankCharIndex < remainingFieldContent.length() && Character.isWhitespace(remainingFieldContent.charAt(firstNonBlankCharIndex))) {
            firstNonBlankCharIndex++;
        }
        if (firstNonBlankCharIndex == remainingFieldContent.length()) {
            throw new ModelAnnotationException(
                    String.format("Empty annotation '%s' in property '%s'", annotationName, propertyName));
        }
        final char firstNonBlankChar = remainingFieldContent.charAt(firstNonBlankCharIndex);
        int index = firstNonBlankCharIndex + 1;
        if (firstNonBlankChar != '"' && firstNonBlankChar != '\'') {
            // Simple case: we look for the next whitespace character
            while (index < remainingFieldContent.length() && !Character.isWhitespace(remainingFieldContent.charAt(index))) {
                index++;
            }
            return new AnnotationValue(index, remainingFieldContent.substring(firstNonBlankCharIndex, index));
        }
        // Complex case: we have a value enclosed between " or '
        boolean closingQuoteFound = false;
        while (index < remainingFieldContent.length()) {
            final char currentChar = remainingFieldContent.charAt(index);
            index++;
            if (currentChar == firstNonBlankChar) {
                closingQuoteFound = true;
                break;
            } else if (currentChar == '\\' && index < remainingFieldContent.length() && remainingFieldContent.charAt(index) == firstNonBlankChar) {
                // Tricky case of the escaped quote: not only do we want to go on reading the string, we also want to remove the backslash
                final int len = remainingFieldContent.length();
                remainingFieldContent = remainingFieldContent.substring(0, index - 1) + remainingFieldContent.substring(index, len);
            }
        }
        if (!closingQuoteFound) {
            throw new ModelAnnotationException(
                    String.format("Non-closing '%s'-escaped value found for annotation '%s' in property '%s'", firstNonBlankChar, annotationName, propertyName));
        }
        return new AnnotationValue(index, remainingFieldContent.substring(firstNonBlankCharIndex + 1, index - 1));
    }

    private static class AnnotationValue {
        final int endPosition;
        final String content;

        private AnnotationValue(int endPosition, String content) {
            this.endPosition = endPosition;
            this.content = content;
        }
    }

    public final String getName() {
        return name;
    }

    public final String getComment() {
        return comment;
    }

    public final boolean isRequired() {
        return required;
    }

    public final String getDefaultValue() {
        return defaultValue;
    }

    public final String getPattern() {
        return pattern;
    }

    public boolean isPassword() {
        return password;
    }

    @Override
    public int hashCode() {
        return Objects.hash(comment, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Property other = (Property) obj;
        return Objects.equals(this.comment, other.comment)
                && Objects.equals(this.name, other.name);
    }
}
