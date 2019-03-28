package org.hesperides.core.application.files;

public class InfiniteMustacheRecursion extends RuntimeException {
    public InfiniteMustacheRecursion(String errorMsg) {
        super(errorMsg);
    }
}
