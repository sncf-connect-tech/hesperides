package org.hesperides.core.infrastructure.security.groups;

class ParentGroupRecursionException extends RuntimeException {
    ParentGroupRecursionException(String msg) {
        super(msg);
    }
}
