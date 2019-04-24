package org.hesperides.core.domain.templatecontainers.exceptions;

public class InvalidTemplateException extends RuntimeException {

    public InvalidTemplateException(String templateKey, String filename, String fieldName, Throwable cause) {
        super(String.format("Invalid field \"%s\" in template %s for module %s", fieldName, filename, templateKey), cause);
    }

    public InvalidTemplateException(String moduleKey, Throwable cause) {
        super(String.format("Invalid template in module %s", moduleKey), cause);
    }
}
