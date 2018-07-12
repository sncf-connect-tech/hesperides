package org.hesperides.core.presentation.io;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OnlyPrintableCharactersValidator implements ConstraintValidator<OnlyPrintableCharacters, String> {
    @Override
    public void initialize(OnlyPrintableCharacters constraintAnnotation) {
        // no-op
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        final int len = value == null ? 0 : value.length();
        for (int i = 0; i < len; i++) {
            if (value.charAt(i) < 0x21) {
                return false;
            }
        }
        // so null and empty strings are considered VALID by this validation, as recommended
        // (http://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-constraint-validator)
        return true;
    }
}
