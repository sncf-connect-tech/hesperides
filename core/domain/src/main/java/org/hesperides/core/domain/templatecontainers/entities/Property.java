package org.hesperides.core.domain.templatecontainers.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.commons.spring.SpringProfiles;
import org.hesperides.core.domain.templatecontainers.exceptions.RequiredPropertyCannotHaveDefaultValueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
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

    public static Property extractProperty(String propertyDefinition) {
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
                    // on considère que le début de la chaîne est le commentaire,
                    comment = extractValueBeforeFirstKnownAnnotation(propertyAnnotations);
                }

                String[] splitAnnotations = splitAnnotationsButKeepDelimiters(propertyAnnotations);
                for (String annotationDefinition : splitAnnotations) {

                    if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.IS_REQUIRED)) {
                        validateIsBlank(extractAnnotationValueLegacyStyle(annotationDefinition));
                        isRequired = true;

                    } else if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.COMMENT)) {
                        validateIsBlank(comment);
                        comment = extractAnnotationValueLegacyStyle(annotationDefinition);

                    } else if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.DEFAULT_VALUE)) {
                        validateIsBlank(defaultValue);
                        defaultValue = extractAnnotationValueLegacyStyle(annotationDefinition);
                        validateIsNotBlank(defaultValue);
                        validateDoesntStartWithArobase(defaultValue);

                    } else if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.PATTERN)) {
                        validateIsBlank(pattern);
                        pattern = extractAnnotationValueLegacyStyle(annotationDefinition);
                        validateIsNotBlank(pattern);
                        validateDoesntStartWithArobase(pattern);

                    } else if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.IS_PASSWORD)) {
                        validateIsBlank(extractAnnotationValueLegacyStyle(annotationDefinition));
                        isPassword = true;

                    } else {
//                        if (extractAnnotationValueLegacyStyle(annotationDefinition) != null) {
//                            throw new IllegalArgumentException();
//                        }
                    }
                }
            }

            validateRequiredOrDefaultValue(name, isRequired, defaultValue);

            property = new Property(name, isRequired, comment, defaultValue, pattern, isPassword);
        }
        return property;
    }

    private static void validateRequiredOrDefaultValue(String name, boolean isRequired, String defaultValue) {
        if (!HasProfile.dataMigration() && isRequired && !StringUtils.isEmpty(defaultValue)) {
            throw new RequiredPropertyCannotHaveDefaultValueException(name);
        }
    }

    private static void validateIsBlank(String value) {
        if (!HasProfile.dataMigration() && StringUtils.isNotBlank(value)) {
            throw new IllegalArgumentException();
        }
    }

    private static void validateIsNotBlank(String value) {
        if (!HasProfile.dataMigration() && StringUtils.isBlank(value)) {
            throw new IllegalArgumentException();
        }
    }

    private static void validateDoesntStartWithArobase(String value) {
        if (!HasProfile.dataMigration() && value != null && value.startsWith("@")) {
            throw new IllegalArgumentException();
        }
    }

    public static boolean startsWithKnownAnnotation(String value) {
        return value
                .trim()
                .toLowerCase()
                // On met un espace après comment, default et pattern pour résoudre le problème de diff décrit dans l'issue 307
                .matches("^(@required|@comment |@default |@pattern |@password).*");
    }

    /**
     * Extrait la valeur d'une chaîne de caractères se trouvant avant la première annotation connue.
     * Si la chaîne de caractères passée en paramètre ne contient pas d'annotation connue,
     * la valeur du retour est celle du paramètre en entrée, sauf si elle est vide (blank).
     * Dans ce cas on retourne null.
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
        return StringUtils.isBlank(result) ? null : result.trim();
    }

    private static String[] splitAnnotationsButKeepDelimiters(String propertyAnnotations) {
        return propertyAnnotations.split("(?=@required|@comment |@default |@pattern |@password)");
    }

    private static boolean annotationDefinitionStartsWith(String annotationDefinition, AnnotationType annotationType) {
        // L'espace optionnel permet de résoudre le diff décrit dans l'issue 307
        String optionalSpace = annotationType.equals(AnnotationType.COMMENT) || annotationType.equals(AnnotationType.DEFAULT_VALUE) || annotationType.equals(AnnotationType.PATTERN) ? " " : "";
        return annotationDefinition.toLowerCase().startsWith("@" + annotationType.getName().toLowerCase() + optionalSpace);
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
        if (StringUtils.isEmpty(result)) {
            result = null;
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
            if (value.startsWith("\"")) {
                result = extractBetweenFirstAndLast(value, "\"");
            } else if (value.startsWith("'")) {
                result = extractBetweenFirstAndLast(value, "'");
            }
            if (result != null) {
                result = someKindOfEscape(result);
            }
        }
        return result;
    }

    private static String extractBetweenFirstAndLast(String value, String character) {
        String result = null;
        int first = value.indexOf(character);
        int last = value.lastIndexOf(character);
        if (first != last) {
            result = value.substring(first + 1, last);
        }
        return result;
    }

    /**
     * Méthode récupérée telle quelle du legacy et très légèrement modifiée (les 3 premières ligne et la dernière)
     * pour la faire fonctionner sans trop savoir comment. À ce niveau c'est de l'humour ^^
     * Si quelqu'un a un peu de temps pour la comprendre et la refaire, bon courage.
     */
    private static String someKindOfEscape(String str) {
        str = "\"" + str + "\"";
        int start = 0;
        int len = str.length();
        // Char to protected string
        final char protectedChar = str.charAt(start);
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

    /**
     * Bidouille permettant de détecter un profil dans un contexte statique.
     * Utilisée pour éviter la validation des propriétés lors de la migration de données.
     */
    @Component
    private static class HasProfile {

        private static Environment staticEnvironment;

        @Autowired
        private Environment environment;

        @PostConstruct
        public void init() {
            staticEnvironment = environment;
        }

        public static boolean dataMigration() {
            boolean isDataMigration = false;
            if (staticEnvironment != null && Arrays.asList(staticEnvironment.getActiveProfiles()).contains(SpringProfiles.DATA_MIGRATION)) {
                isDataMigration = true;
            }
            return isDataMigration;
        }
    }
}
