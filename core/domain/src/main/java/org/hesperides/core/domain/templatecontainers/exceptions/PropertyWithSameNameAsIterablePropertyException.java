package org.hesperides.core.domain.templatecontainers.exceptions;

public class PropertyWithSameNameAsIterablePropertyException extends IllegalArgumentException {
    public PropertyWithSameNameAsIterablePropertyException(String templateKey, String filename, String propertyName) {
        super(String.format("Can't use a property with the same name as the Iterable Property \"%s\" in template %s for module %s",
                propertyName, filename, templateKey));
    }
}
