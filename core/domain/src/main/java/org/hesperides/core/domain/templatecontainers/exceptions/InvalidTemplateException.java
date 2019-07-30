package org.hesperides.core.domain.templatecontainers.exceptions;

public class InvalidTemplateException extends IllegalArgumentException {

    public InvalidTemplateException(String templateKey, String filename, String fieldName, Throwable cause) {
        super(String.format("Invalid field \"%s\" in template %s for module %s : %s",
                fieldName, filename, templateKey, cause.getMessage()), cause);
    }

}
