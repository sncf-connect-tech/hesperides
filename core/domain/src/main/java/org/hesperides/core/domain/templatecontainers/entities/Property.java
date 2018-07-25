package org.hesperides.core.domain.templatecontainers.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.templatecontainers.exceptions.RequiredPropertyCannotHaveDefaultValueException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@EqualsAndHashCode(callSuper = true)
public class Property extends AbstractProperty {

    boolean isRequired;
    String comment;
    String defaultValue;
    String pattern;
    boolean isPassword;

    public Property(String name, boolean isRequired, String comment, String defaultValue, String pattern, boolean isPassword) {
        super(name);
        this.isRequired = isRequired;
        this.comment = comment;
        this.defaultValue = defaultValue;
        this.pattern = pattern;
        this.isPassword = isPassword;
    }


    public void validate() {
        if (isRequired && !StringUtils.isEmpty(defaultValue)) {
            throw new RequiredPropertyCannotHaveDefaultValueException(getName());
        }
    }

    public enum AnnotationType {
        IS_REQUIRED("required"),
        COMMENT("comment"),
        DEFAULT_VALUE("default"),
        PATTERN("pattern"),
        IS_PASSWORD("password");

        private final String name;

        AnnotationType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static final String NAME_ANNOTATIONS_SEPARATOR_REGEX = "[|]";
    private static final int NAME_INDEX = 0;
    private static final int ANNOTATIONS_INDEX = 1;

    public static Property extractPropertyFromStringDefinition(String propertyDefinition) {
        Property property = null;
        if (propertyDefinition != null) {
            String[] propertyAttributes = propertyDefinition.split(NAME_ANNOTATIONS_SEPARATOR_REGEX, 2);

            String name = propertyAttributes[NAME_INDEX].trim();
            // Valeurs par défaut
            boolean isRequired = false;
            String comment = "";
            String defaultValue = "";
            String pattern = "";
            boolean isPassword = false;

            if (propertyAttributes.length > 1) {
                comment = null;

                String propertyAnnotations = propertyAttributes[ANNOTATIONS_INDEX];
                if (!startsWithKnownAnnotation(propertyAnnotations)) {
                    // Si la valeur des annotations ne commence pas avec une annotation connue,
                    // on considère que le début de chaîne est le commentaire,
                    // potentiellement écrasé après.
                    comment = extractValueBeforeFirstKnownAnnotation(propertyAnnotations);
                }

                String[] splitAnnotations = splitAnnotationsButKeepDelimiters(propertyAnnotations.trim());
                for (String annotationDefinition : splitAnnotations) {

                    if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.IS_REQUIRED)) {
                        isRequired = true;

                    } else if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.COMMENT)) {
                        comment = extractAnnotationValueLegacyStyle(annotationDefinition);

                    } else if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.DEFAULT_VALUE)) {
                        defaultValue = extractAnnotationValueLegacyStyle(annotationDefinition);

                    } else if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.PATTERN)) {
                        pattern = extractAnnotationValueLegacyStyle(annotationDefinition);

                    } else if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.IS_PASSWORD)) {
                        isPassword = true;
                    } else {
                        //TODO Throw exception
                    }
                }
            }
            property = new Property(name, isRequired, comment, defaultValue, pattern, isPassword);
        }
        return property;
    }

    public static boolean startsWithKnownAnnotation(String value) {
        return value
                .trim()
                .toLowerCase()
                .matches("^(@required|@comment|@default|@pattern|@password).*");
    }

    /**
     * Extrait la valeur d'une chaîne de caractères se trouvant avant la première annotation connue.
     * Si la chaîne de caractères passée en paramètre ne contient pas d'annotation connue,
     * la valeur du retour est celle du paramètre en entrée.
     */
    public static String extractValueBeforeFirstKnownAnnotation(String value) {
        String result;
        Matcher matcher = Pattern.compile("@required|@comment|@default|@pattern|@password").matcher(value);
        if (matcher.find()) {
            int indexOfFirstKnownAnnotation = matcher.start();
            result = value.substring(0, indexOfFirstKnownAnnotation);
        } else {
            result = value;
        }
        return result.trim();
    }

    private static String[] splitAnnotationsButKeepDelimiters(String propertyAnnotations) {
        return propertyAnnotations.split("(?=@required|@comment|@default|@pattern|@password)");
    }

    private static boolean annotationDefinitionStartsWith(String annotationDefinition, AnnotationType annotationType) {
        return annotationDefinition.toLowerCase().startsWith("@" + annotationType.getName().toLowerCase());
    }

    /**
     * Récupère la valeur d'une annotation. Si cette valeur est entre guillemets (simples ou doubles),
     * on renvoie tout ce qu'il y a entre guillemets, sinon juste le premier mot.
     * S'il n'y a qu'un seul guillemet au début de la valeur, on retourne null.
     */
    public static String extractAnnotationValueLegacyStyle(String annotationDefinition) {
        String result;
        String valueAfterFirstSpace = extractValueAfterFirstSpace(annotationDefinition);
        if (startsWithQuotes(valueAfterFirstSpace)) {
            result = extractValueBetweenQuotes(valueAfterFirstSpace);
        } else {
            result = extractFirstWord(valueAfterFirstSpace);
        }
        if (result != null) {
            result = result.trim();
        }
        return result;
    }

    public static String extractValueAfterFirstSpace(String value) {
        String result = null;
        if (value != null) {
            String trimmedValue = value.trim();
            int indexOfFirstSpace = trimmedValue.indexOf(" ");
            if (indexOfFirstSpace != -1) {
                result = trimmedValue.substring(indexOfFirstSpace).trim();
            }
        }
        return result;
    }

    /**
     * Extrait la valeur entres guillemets (simples ou doubles).
     */
    public static String extractValueBetweenQuotes(String value) {
        String result = null;
        if (value != null) {
            result = StringUtils.substringBetween(value, "\"", "\"");
            if (result == null) {
                result = StringUtils.substringBetween(value, "'", "'");
            }
        }
        return result;
    }

    public static boolean startsWithQuotes(String value) {
        return value != null && (value.trim().startsWith("\"") || value.trim().startsWith("'"));
    }

    private static String extractFirstWord(String value) {
        String result = null;
        if (value != null) {
            String trimmedValue = value.trim();
            int indexOfFirstSpace = trimmedValue.indexOf(" ");
            if (indexOfFirstSpace != -1) {
                result = trimmedValue.substring(0, indexOfFirstSpace).trim();
            } else {
                result = trimmedValue;
            }
        }
        return result;
    }
}
