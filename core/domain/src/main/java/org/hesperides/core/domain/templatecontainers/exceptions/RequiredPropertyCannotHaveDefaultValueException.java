package org.hesperides.core.domain.templatecontainers.exceptions;

public class RequiredPropertyCannotHaveDefaultValueException extends RuntimeException {

    public RequiredPropertyCannotHaveDefaultValueException(String propertyName) {
        super(String.format("Property %s cannot have both annotations @required and @default", propertyName));
    }
}
