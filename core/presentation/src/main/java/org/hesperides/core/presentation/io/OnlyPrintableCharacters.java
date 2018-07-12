package org.hesperides.core.presentation.io;


import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Vérifie que la propriété annotée ne contient pas de caractère invalide (&lt; 0x20).
 * Cette annotation hérite de @NotEmpty qui hérite elle-même de @NotNull.
 * La propriété doit donc est renseignée et valide.
 *
 * @since issue #232
 */
@Documented
@Constraint(validatedBy = {OnlyPrintableCharactersValidator.class})
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
@ReportAsSingleViolation
@NotEmpty
public @interface OnlyPrintableCharacters {
    /**
     * @return validation target, used in error message if value is found invalid
     */
    String subject();

    String message() default "{subject} contains an invalid character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
