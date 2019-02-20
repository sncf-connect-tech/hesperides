package org.hesperides.core.domain.platforms.exceptions;

public class PropertyPatternNotMatchedException extends InvalidPropertyValorisationException {

    public PropertyPatternNotMatchedException(String propertyName, String pattern) {
        super(String.format("The value of the property \"%s\" doesn't match the pattern \"%s\"", propertyName, pattern));
    }
}
