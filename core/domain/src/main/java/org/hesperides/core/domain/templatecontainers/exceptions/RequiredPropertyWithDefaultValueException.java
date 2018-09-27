package org.hesperides.core.domain.templatecontainers.exceptions;

public class RequiredPropertyWithDefaultValueException extends RuntimeException {

    public RequiredPropertyWithDefaultValueException(String propertyName) {
        super(String.format("Property %s cannot have both annotations @required and @default", propertyName));
    }
}
