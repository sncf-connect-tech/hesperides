package org.hesperides.tests.bdd.commons;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class CommonSteps extends HesperidesScenario implements En {

    public CommonSteps() {

        Then("^the request is rejected with a bad request error$", () -> {
            assertBadRequest();
        });

        Then("^an empty list is returned$", () -> {
            assertOK();
            assertEquals(0, getBodyAsArray().length);
        });
    }
}
