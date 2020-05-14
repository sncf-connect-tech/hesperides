package org.hesperides.test.bdd.configuration;

import org.hesperides.commons.SpringProfiles;
import org.hesperides.core.presentation.exceptions.GlobalExceptionHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Profile(SpringProfiles.TEST)
public class TestExceptionHandler extends GlobalExceptionHandler {
    @Override
    protected void beforeHandling(Exception exception) {
        exception.printStackTrace();
    }
}
