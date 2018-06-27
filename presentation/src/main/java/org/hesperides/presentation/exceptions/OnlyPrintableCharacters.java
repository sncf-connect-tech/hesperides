package org.hesperides.presentation.exceptions;


import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Asserts that the annotated string does not contain any non printable characters (< 0x20).
 *
 * @since issue #232
 */
@Documented
@Constraint(validatedBy = { OnlyPrintableCharactersValidator.class })
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
@ReportAsSingleViolation
@NotEmpty
public @interface OnlyPrintableCharacters {
    /**
     * validation target, used in error message if value is found invalid
     */
    String subject();

    String message() default "{subject} contains an invalid character";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
