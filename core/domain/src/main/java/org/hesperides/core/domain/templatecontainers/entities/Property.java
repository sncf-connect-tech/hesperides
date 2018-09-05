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

    private enum AnnotationType {
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

    private static final String PIPE_REGEX = "[|]";
    private static final int NAME_INDEX = 0;
    private static final int ANNOTATIONS_INDEX = 1;

    public static Property extractProperty(String propertyDefinition) {
        Property property = null;
        if (propertyDefinition != null) {
            String[] propertyAttributes = propertyDefinition.split(PIPE_REGEX, 2);

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

                    if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.IS_REQUIRED, propertyAnnotations)) {
                        // #318
                        if (annotationIsFollowedBySpace(AnnotationType.IS_REQUIRED, propertyAnnotations)) {
                            validateIsBlank(extractAnnotationValueLegacyStyle(annotationDefinition));
                            isRequired = true;
                        }

                    } else if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.COMMENT, propertyAnnotations)) {
                        validateIsBlank(comment);
                        // #311
                        if (onlyStartsWithQuotes(extractValueAfterFirstSpace(annotationDefinition))) {
                            // #324
                            String valueBetweenQuotes = extractCommentThatStartsWithQuotes(propertyAnnotations);
                            if (valueBetweenQuotes != null) {
                                comment = valueBetweenQuotes;
                            }
                            break;
                        } else {
                            comment = extractAnnotationValueLegacyStyle(annotationDefinition);
                        }
                        // #317
                        if (comment != null) {
                            comment = comment.trim();
                        }

                    } else if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.DEFAULT_VALUE, propertyAnnotations)) {
                        validateIsBlank(defaultValue);
                        defaultValue = extractAnnotationValueLegacyStyle(annotationDefinition);
                        validateIsNotBlank(defaultValue);
                        validateDoesntStartWithArobase(defaultValue);

                    } else if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.PATTERN, propertyAnnotations)) {
                        validateIsBlank(pattern);
                        pattern = extractAnnotationValueLegacyStyle(annotationDefinition);
                        validateIsNotBlank(pattern);
                        validateDoesntStartWithArobase(pattern);

                    } else if (annotationDefinitionStartsWith(annotationDefinition, AnnotationType.IS_PASSWORD, propertyAnnotations)) {
                        // #318
                        if (annotationIsFollowedBySpace(AnnotationType.IS_PASSWORD, propertyAnnotations)) {
                            validateIsBlank(extractAnnotationValueLegacyStyle(annotationDefinition));
                            isPassword = true;
                        }
                    }
                }
            }

            validateRequiredOrDefaultValue(name, isRequired, defaultValue);

            property = new Property(name, isRequired, comment, defaultValue, pattern, isPassword);
        }
        return property;
    }

    /**
     * #318
     */
    private static boolean annotationIsFollowedBySpace(AnnotationType annotationType, String propertyAnnotations) {
        return Pattern.compile("(?i)(@" + annotationType.getName() + ")(\\s|$)").matcher(propertyAnnotations).find();
    }

    private static boolean onlyStartsWithQuotes(String value) {
        return value != null &&
                ((value.startsWith("'") && value.substring(1).indexOf("'") == -1) ||
                        (value.startsWith("\"") && value.substring(1).indexOf("\"") == -1));
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

    static boolean startsWithKnownAnnotation(String value) {
        return value
                .trim()
                .toLowerCase()
                // On met un espace après comment, default et pattern pour résoudre le problème de diff décrit dans l'issue 307
                .matches("^(@required|@comment |@default |@pattern |@password).*");
    }

    /**
     * Extrait la valeur d'une chaîne de caractères se trouvant avant la première annotation connue.
     * Si la chaîne de caractères passée en paramètre ne contient pas d'annotation connue,
     * la valeur du retour est celle du paramètre en entrée,
     * sauf si elle contient une arobase,
     * et sauf si elle est vide (blank) auquel cas on retourne null.
     * Enfin, presque.
     * <p>
     * Il ne faut pas chercher à comprendre ce que fait cette méthode,
     * on reproduit le comportement du legacy pour répondre aux cas d'utilisation
     * décrits dans le test unitaire PropertyTest::oldCommentArobase.
     */
    static String extractValueBeforeFirstKnownAnnotation(String value) {
        String result;
        Matcher matcher = Pattern.compile("(?i)(@required|@comment|@default|@pattern|@password)").matcher(value);
        if (matcher.find()) {
            int indexOfFirstKnownAnnotation = matcher.start();
            result = value.substring(0, indexOfFirstKnownAnnotation);
        } else {
            result = value;
        }
        result = extractValueBeforeFirstArobase(result); // #312
        result = StringUtils.isBlank(result) ? null : result.trim();
        // #312
        if (result != null && result.startsWith("@") && !result.equals(value.trim())) {
            result = null;
        }
        return result;
    }

    /**
     * Legacy :
     * <p>
     * Extrait la valeur d'une chaîne de caractères se trouvant avant la première arobase.
     * Si l'arobase est le premier caractère et si le mot après l'arobase n'est pas la fin de la chaîne ou ne se termine pas par un espace,
     * on retourne null.
     * Si la valeur passée en paramètre ne contient pas d'arobase,
     * ou si l'arobase est le premier caractère,
     * ou s'il y a un espace juste avant la première arobase et si le mot après l'arobase est la fin de la chaîne ou se termine par un espace,
     * on retourne la valeur passée en paramètre telle quelle.
     */
    private static String extractValueBeforeFirstArobase(String value) {
        String result = null;
        if (value != null) {
            int firstArobase = value.indexOf("@");
            if (firstArobase > -1) {
                if (firstArobase == 0 && !arobaseEndsWithSpaceOrIsTheEnd(value.substring(firstArobase))) {
                    result = null;
                } else if (firstArobase == 0 || value.charAt(firstArobase - 1) == ' ' && arobaseEndsWithSpaceOrIsTheEnd(value.substring(firstArobase))) {
                    result = value;
                } else {
                    result = value.substring(0, firstArobase);
                }
            } else {
                result = value;
            }
        }
        return result;
    }

    static boolean arobaseEndsWithSpaceOrIsTheEnd(String value) {
        return Pattern.compile("^@[a-zA-Z]*( |$)").matcher(value).find();
    }

    /**
     * Les annotations doivent être précédées d'un espace pour être détecté,
     * sauf dans le cas de la première annotation.
     */
    private static String[] splitAnnotationsButKeepDelimiters(String propertyAnnotations) {
        return propertyAnnotations.split("(^| )((?i)(?=@required|@comment |@default |@pattern |@password))");
    }

    private static boolean annotationDefinitionStartsWith(String annotationDefinition, AnnotationType annotationType, String propertyAnnotations) {
        // L'espace optionnel permet de résoudre le diff décrit dans l'issue 307
        String optionalSpace = annotationType.equals(AnnotationType.COMMENT) || annotationType.equals(AnnotationType.DEFAULT_VALUE) || annotationType.equals(AnnotationType.PATTERN) ? " " : "";
        return annotationDefinition.toLowerCase().startsWith("@" + annotationType.getName().toLowerCase() + optionalSpace) &&
                isFirstAnnotationOrAfterSpace(annotationType, propertyAnnotations);
    }

    /**
     * Résoud les issues #314 et #315 :
     * <p>
     * Dans le legacy, les annotations doivent commencer après un espace,
     * sauf la première qui peut commencer après le pipe qui sépare le nom
     * de la propriété et la définition des annotations.
     */
    private static boolean isFirstAnnotationOrAfterSpace(AnnotationType annotationType, String propertyAnnotations) {
        int indexOfAnnotation = propertyAnnotations.toLowerCase().indexOf("@" + annotationType.getName().toLowerCase());
        return indexOfAnnotation == 0 || propertyAnnotations.charAt(indexOfAnnotation - 1) == ' ';
    }

    /**
     * Récupère la valeur d'une annotation. Si cette valeur est entre guillemets (simples ou doubles),
     * on renvoie tout ce qu'il y a entre guillemets, sinon juste le premier mot.
     * S'il n'y a qu'un seul guillemet au début de la valeur, on retourne null.
     */
    static String extractAnnotationValueLegacyStyle(String annotationDefinition) {
        String result;
        String valueAfterFirstSpace = extractValueAfterFirstSpace(annotationDefinition);
        if (startsWithQuotes(valueAfterFirstSpace)) {
            result = extractValueBetweenQuotes(valueAfterFirstSpace);
        } else {
            result = extractFirstWord(valueAfterFirstSpace);
        }
        if (StringUtils.isEmpty(result)) {
            result = null;
        }
        return result;
    }

    private static String extractValueAfterFirstSpace(String value) {
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
    static String extractValueBetweenQuotes(String value) {
        String result = null;
        if (value != null) {
            if (value.startsWith("\"")) {
                result = extractBetweenFirstAndNextUnescapedQuotes(value, "\"");
            } else if (value.startsWith("'")) {
                result = extractBetweenFirstAndNextUnescapedQuotes(value, "'");
                if (result != null) {
                    // #320
                    result = result.replaceAll("\\\"", "\\\\\"");
                }
            }
            if (result != null) {
                result = legacyEscape(result);
            }
        }
        return result;
    }

    /**
     * 324
     */
    private static String extractCommentThatStartsWithQuotes(String propertyAnnotations) {
        String annotation = "@comment ";
        String substring = propertyAnnotations.substring(propertyAnnotations.indexOf(annotation) + annotation.length());
        return extractValueBetweenQuotes(substring);
    }

    private static String extractBetweenFirstAndNextUnescapedQuotes(String value, String character) {
        String result = null;
        int first = value.indexOf(character);
        int next = getNextUnescapedCharacterPosition(value, first + 1, character);
        if (next > first) {
            result = value.substring(first + 1, next);
        }
        return result;
    }

    /**
     * #321
     */
    private static int getNextUnescapedCharacterPosition(String value, int begin, String character) {
        int position = value.indexOf(character, begin);
        if (position > 0 && value.charAt(position - 1) == '\\') {
            position = getNextUnescapedCharacterPosition(value, position + 1, character);
        }
        return position;
    }

    /**
     * Méthode récupérée telle quelle du legacy et très légèrement modifiée (les 3 premières ligne et la dernière)
     * pour la faire fonctionner sans trop savoir comment. À ce niveau c'est de l'humour ^^
     * Si quelqu'un a un peu de temps pour la comprendre et la refaire, bon courage.
     */
    private static String legacyEscape(String str) {
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

    private static boolean startsWithQuotes(String value) {
        return value != null && (value.trim().startsWith("\"") || value.trim().startsWith("'"));
    }

    private static String extractFirstWord(String value) {
        String result = null;
        if (value != null) {
            result = value.trim().split("\\s")[0];
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
