package org.hesperides.core.domain.platforms.exceptions;

public class RequiredPropertyNotValorisedException extends InvalidPropertyValorisationException {

    public RequiredPropertyNotValorisedException(String propertyName) {
        super(String.format("The value of property %s is required", propertyName));
    }
}
