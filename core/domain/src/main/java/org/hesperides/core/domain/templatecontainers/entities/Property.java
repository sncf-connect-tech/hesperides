package org.hesperides.core.domain.templatecontainers.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.core.domain.templatecontainers.exceptions.RequiredPropertyCannotHaveDefaultValueException;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Optional;
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

        public static Optional<AnnotationType> fromName(String name) {
            return Arrays.stream(AnnotationType.values())
                    .filter(annotationType -> annotationType.getName().equalsIgnoreCase(name))
                    .findFirst();
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
                // Détecte la ou les annotations utilisées
                if (!containsAnnotations(propertyAnnotations)) {
                    // S'il n'y en a pas, c'est un commentaire brut
                    comment = propertyAnnotations;
                } else {
                    // Sinon, on récupère la valeur à partir du premier espace jusqu'à la fin de la chaîne
                    // Mais dans le legacy, on récupère la valeur entre guillemets ou le premier mot s'il n'y a pas de guillemets...
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
                            comment = annotationDefinition.trim();
                        }
                    }
                }
            }
            property = new Property(name, isRequired, comment, defaultValue, pattern, isPassword);
        }
        return property;
    }

    private static boolean containsAnnotations(String propertyAnnotations) {
        return propertyAnnotations.toLowerCase().matches(".*(@required|@comment|@default|@pattern|@password).*");
    }

    private static String[] splitAnnotationsButKeepDelimiters(String propertyAnnotations) {
        return propertyAnnotations.split("(?=@required|@comment|@default|@pattern|@password)");
    }

    private static boolean annotationDefinitionStartsWith(String annotationDefinition, AnnotationType annotationType) {
        return annotationDefinition.toLowerCase().startsWith("@" + annotationType.getName().toLowerCase());
    }

    /**
     * Récupère la valeur entre le premier espace et la fin de la chaîne de caractère passée en paramètre
     */
    public static String extractAnnotationValue(String annotationDefinition) {
        int indexOfFirstSpace = annotationDefinition.indexOf(" ");
        String valueThatMayBeSurroundedByQuotes = annotationDefinition.substring(indexOfFirstSpace);
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

    /**
     * Récupère la valeur entre guillemets ou le premier mot s'il n'y a pas de guillemets.
     * Mais s'il n'y a qu'une seule guillemet au début de la valeur, on retourne null.
     * <p>
     * Ce bout de code est infâme. Le but est de reproduire le comportement hérétique du legacy.
     * À terme, l'idée est de le supprimer mais cela nécessite une
     */
    public static String extractAnnotationValueLegacyStyle(String annotationDefinition) {
        String result;

        int indexOfFirstSpace = annotationDefinition.indexOf(" ");
        String valueThatMayBeSurroundedByQuotes = annotationDefinition.substring(indexOfFirstSpace).trim();
        String valueContainedInsideQuotes = extractValueContainedInsideQuotes(valueThatMayBeSurroundedByQuotes);

        if (valueContainedInsideQuotes == null && !valueThatMayBeSurroundedByQuotes.startsWith("\"")) {
            if (valueThatMayBeSurroundedByQuotes.indexOf(" ") != -1) {
                result = valueThatMayBeSurroundedByQuotes.substring(0, valueThatMayBeSurroundedByQuotes.indexOf(" "));
            } else {
                result = valueThatMayBeSurroundedByQuotes;
            }
        } else {
            result = valueContainedInsideQuotes;
        }
        return result;
    }

    private static String extractValueContainedInsideQuotes(String value) {
        String result = null;
        Matcher matcher = Pattern.compile("\"([^\"]*)\"").matcher(value);
        while (matcher.find()) {
            result = matcher.group(1);
            break;
        }
        return result;
    }
}
