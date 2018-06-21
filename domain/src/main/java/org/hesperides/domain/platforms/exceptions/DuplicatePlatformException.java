package org.hesperides.domain.platforms.exceptions;

import org.hesperides.domain.exceptions.DuplicateException;
import org.hesperides.domain.platforms.entities.Platform;

public class DuplicatePlatformException extends DuplicateException {
    public DuplicatePlatformException(Platform.Key platformKey) {
        super("could not create a new platform with key: " + platformKey + " as it already exists");
    }
}
