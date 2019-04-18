package org.hesperides.core.domain.platforms.exceptions;

import org.hesperides.core.domain.exceptions.DuplicateException;

public class DuplicateDeployedModuleIdException extends DuplicateException {

    public DuplicateDeployedModuleIdException() {
        super("Trying to create or update a platform using the same id for multiple modules");
    }
}
