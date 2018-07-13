package org.hesperides.core.domain.platforms.exceptions;

import org.hesperides.core.domain.exceptions.DuplicateException;
import org.hesperides.core.domain.platforms.entities.Platform;

public class DuplicatePlatformException extends DuplicateException {
    public DuplicatePlatformException(Platform.Key platformKey) {
        super("could not create a new platform with key: " + platformKey + " as it already exists");
    }
}
