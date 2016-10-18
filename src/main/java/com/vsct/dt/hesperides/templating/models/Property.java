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
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"name", "comment", "required", "defaultValue", "pattern"})
public class Property {
    private boolean password;
    private String comment;
    private final String name;
    private boolean required;
    private String defaultValue;
    private String pattern;

    private static final Logger LOGGER = LoggerFactory.getLogger(Property.class);

    public Property(final DefaultCode code) {
        /* Bad but no other way for now */
        /* TO DO BIG WARNING */
        try {
            Field f = DefaultCode.class.getDeclaredField("name");
            f.setAccessible(true);
            String nameAndCommentString = (String) f.get(code);
            String[] fields = nameAndCommentString.split("[|]", 2);

            // We trim the name : Hesperides should ignore tabs, whitespaces on property names
            this.name = fields[0].trim();

            // Second field can be comment (old way) or annotation (new way).
            if (fields.length > 1) {

                List<HesperidesAnnotation> annotationList = splitByAnnotation(fields[1], this.name, this.name.length());

                for (HesperidesAnnotation annotation : annotationList) {
                    if (!annotation.isValid()) {
                        throw new ModelAnnotationException(
                                String.format("Annotation '@%s' is not valid for property '%s'. Please check it !",
                                        annotation.getName(), this.name));
                    }

                    switch (annotation.getName()) {
                        case "default" :
                            this.defaultValue = manageAnnotation(this.defaultValue, annotation);

                            if (this.required) {
                                throwRequiriedAndDefaultInSameTime();
                            }

                            break;
                        case "pattern" :
                            this.pattern = manageAnnotation(this.pattern, annotation);
                            break;
                        case "required" :
                            if (this.defaultValue != null) {
                                throwRequiriedAndDefaultInSameTime();
                            }

                            this.required = true;
                            break;
                        case "password" :
                            this.password = true;
                            break;
                        case "comment" :
                            manageComment(annotation);

                            break;
                        default:
                            throw new ModelAnnotationException(
                                    String.format("Annotation '%s' is not manager by property but Hesperides know it !",
                                            annotation.getName()));
                    }
                }
            } else {
                this.comment = "";
            }

            if (this.defaultValue == null) {
                this.defaultValue = "";
            }

            if (this.pattern == null) {
                this.pattern = "";
            }
        } catch (final IllegalAccessException | NoSuchFieldException e) {
            LOGGER.debug(e.toString());
            throw new RuntimeException(e);
        }
    }

    @JsonCreator
    public Property(@JsonProperty("name") final String name,
                    @JsonProperty("comment") final String comment) {
        this.name = name;
        this.comment = comment;
    }

    /**
     * Throw exception.
     */
    private void throwRequiriedAndDefaultInSameTime() {
        throw new ModelAnnotationException(
                String.format("Property '%s' canno't be @require and @default !", this.name));
    }

    /**
     * Manage comment annotation.
     *
     * @param annotation annotation
     */
    private void manageComment(final HesperidesAnnotation annotation) {
        String comment = annotation.getValue();

        if (comment != null && StringUtils.isNotBlank(comment)) {
            if (this.comment != null) {
                throw new ModelAnnotationException(String.format("Many annotation @comment for property '%s'", this.name));
            }

            this.comment = annotation.getValue().trim();
        }
    }

    /**
     * Manage annotation (check if already set).
     *
     * @param oldValue old value
     * @param annotation annotation
     */
    private String manageAnnotation(final String oldValue, final HesperidesAnnotation annotation) {
        if (oldValue != null) {
            throw new ModelAnnotationException(String.format("Many annotation @%s for property '%s'",
                    annotation.getName(), this.name));
        }

        return annotation.getValue();
    }

    /**
     * Get all annotation.
     *
     * @param str string after name of property
     * @param propertyName property name
     * @param offset start position
     *
     * @return list of annotation
     */
    private static List<HesperidesAnnotation> splitByAnnotation(final String str, final String propertyName,
                                                                        final int offset) {
        final List<HesperidesAnnotation> result = new ArrayList<>();

        // Search '@' char but escape in string "" or ''

        final int len = str.length();
        // Indicate first annotation position. -1 Mean that not init.
        int lastAnnotationPos = -1;
        // Current char
        char currentChar;

        // Current annotation name
        TemporaryValueProperty annotation;
        // Value of annotation
        TemporaryValueProperty value;

        for (int index = 0; index < len; index++) {
            currentChar = str.charAt(index);

            if (currentChar == '@') {
                // Not initiate
                if (lastAnnotationPos == -1) {
                    // We initiate it
                    lastAnnotationPos = index;

                    // Copy data like comment
                    result.add(
                            new HesperidesCommentAnnotation(str.substring(0, index)));
                }

                // Old way can be have email in comment
                if (isNotAnnotation(str, len, index)) {
                    continue;
                }

                // Because before we have simple comment, we need check is not email address :-(
                annotation = grabAnnotationName(str, len, index);

                index += annotation.length();

                // We are in annotation and data start by single or double quote.
                value = grapAnnotationValue(str, len, index);

                if (value == null) {
                    // Error
                    throw new ModelAnnotationException(
                            String.format("Invalid parameter at %d for property '%s' with annotation '%s'!",
                                    offset + index, propertyName, annotation));
                }

                HesperidesAnnotation annotationObj = HesperidesAnnotationConstructor.createAnnotationObject(
                        annotation.getValue(), value.getValue());

                index += value.length();

                if (annotationObj == null) {
                    if (result.size() == 1 && result.get(0) instanceof HesperidesCommentAnnotation) {
                        result.remove(0);

                        continue;
                    }

                    throw new ModelAnnotationException(
                            String.format("Invalid annotation name at %d for property '%s' with annotation '%s' !",
                                    offset + index, propertyName, annotation.getValue()));
                }

                result.add(annotationObj);
            }
        }

        if (result.isEmpty()) {
            // Copy data like comment
            result.add(
                    new HesperidesCommentAnnotation(str));
        }

        return result;
    }

    /**
     * Check if it's an email address.
     *
     * @param str string after name of property
     * @param len len string
     * @param arobasePos arobase position
     *
     * @return true/false
     */
    private static boolean isNotAnnotation(final String str, final int len, final int arobasePos) {
        boolean notAnnotation = false;

        // 1 - Check if before '@' found white space
        if (arobasePos > 0 && !Character.isWhitespace(str.charAt(arobasePos - 1))) {
            // Is not annotation
            notAnnotation = true;
        }

        if (arobasePos == len - 1) {
            // Last char !
            notAnnotation = true;
        }

        char currentChar;

        // 2 - Found first first whitespace
        for (int index = arobasePos + 1; index < len && !notAnnotation; index++) {
            // Annotation must be [a-zA-Z]
            currentChar = str.charAt(index);

            // If not A-Z or a-z break
            if (!((currentChar > 0x40 && currentChar < 0x5B) || (currentChar > 0x60 && currentChar < 0x7B))) {
                if (Character.isWhitespace(currentChar)) {
                    break;
                }

                notAnnotation = true;
            }
        }

        return notAnnotation;
    }

    /**
     * Get value of annotation.
     *
     * @param str string
     * @param len len string
     * @param start position to start
     *
     * @return substring of str or empty string if no parameter. If null this is an error.
     */
    private static TemporaryValueProperty grapAnnotationValue(String str, int len, int start) {
        TemporaryValueProperty result;

        if (start < len) {

            // Skip blank
            int startNonBlank = skipWhitespace(str, len, start);

            char currentChar;

            if (startNonBlank < (len - 1)) {
                currentChar = str.charAt(startNonBlank);
            } else {
                currentChar = '\0';
            }

            if (currentChar == '@') {
                // Is new annotation, stop.
                result = new TemporaryValueProperty("", 0);
            } else if (currentChar == '"' || currentChar == '\'') {
                // Copy protected string
                result = copyProtectedString(str, len, startNonBlank);
            } else {
                result = copyFirstWord(str, len, startNonBlank);
            }
        } else {
            result = new TemporaryValueProperty(null, 0);
        }

        return result;
    }

    /**
     * Get protected string by simple or double quote.
     *
     * @param str string
     * @param len len string
     * @param start position to start
     *
     * @return substring of str without protection and escape char.
     */
    private static TemporaryValueProperty copyProtectedString(final String str, final int len, final int start) {
        // Char to protected string
        final char protectedChar = str.charAt(start) ;
        // String content
        String result = null;
        // Current char
        char currentChar;
        // builder
        StringBuilder sb = new StringBuilder(len - start);
        int index;

        for (index = start + 1; index < len && result == null; index++) {
            currentChar = str.charAt(index);

            if (currentChar == '\\') {
                // Escape char
                index++;

                // check if out of bound. For exemple -> "truc \
                if (index < len) {
                    sb.append(str.charAt(index));
                }
            } else if (currentChar == protectedChar) {
                result = sb.toString();
            } else {
                sb.append(str.charAt(index));
            }
        }

        return new TemporaryValueProperty(result, index - start);
    }

    /**
     * Skip white space.
     *
     * @param str string
     * @param len len string
     * @param start position to start
     *
     * @return position of first non space char.
     */
    private static int skipWhitespace(final String str, final int len, final int start) {
        int index;
        // After annotation, we have whitespace.
        boolean skipFirstWhitespace = true;

        for (index = start; index < len && skipFirstWhitespace; index++) {
            // Search blank char
            skipFirstWhitespace = Character.isWhitespace(str.charAt(index));
        }

        // Must decrement to have right position
        return --index;
    }

    /**
     * Get annotation.
     *
     * @param str string
     * @param len len string
     * @param start position to start
     *
     * @return substring of str
     */
    private static TemporaryValueProperty grabAnnotationName(final String str, final int len, final int start) {
        final TemporaryValueProperty tmp = copyFirstWord(str, len, start);
        final String val = tmp.getValue().trim();

        return new TemporaryValueProperty(val, tmp.length());
    }

    /**
     * Copy first word.
     *
     * @param str string
     * @param len len string
     * @param start position to start
     *
     * @return substring of str
     */
    private static TemporaryValueProperty copyFirstWord(final String str, final int len, final int start) {
        // In fact, never return null cause @ is copied.
        String result = null;
        // Last char
        final int lastCharPos = len - 1;

        for (int index = start; index < len && result == null; index++) {
            // Search blank char or if is last char
            if (Character.isWhitespace(str.charAt(index))) {
                result = str.substring(start, index);
            } else if (index == lastCharPos) {
                result = str.substring(start, index + 1);
            }
        }

        return new TemporaryValueProperty(result, result == null ? 0 : result.length());
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
