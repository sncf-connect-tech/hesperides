package org.hesperides.test.bdd.events;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.events.EventOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class GetEvents extends HesperidesScenario implements En {

    @Autowired
    private OldModuleBuilder moduleBuilder;
    @Autowired
    private PlatformBuilder platformBuilder;

    public GetEvents() {

        When("^I( try to)? get the events of this module$", (String tryTo) -> {
            testContext.setResponseEntity(getModuleEvents(moduleBuilder.build(), getResponseType(tryTo, EventOutput[].class)));
        });

        When("^I( try to)? get the events of this platform$", (String tryTo) -> {
            testContext.setResponseEntity(getPlatformEvents(platformBuilder.buildInput(), getResponseType(tryTo, EventOutput[].class)));
        });

        Then("^(\\d+) event(?: is|s are) returned$", (Integer nbEvents) -> {
            assertOK();
            EventOutput[] events = testContext.getResponseBody(EventOutput[].class);
            assertEquals(nbEvents.intValue(), events.length);
        });

        Then("^event at index (\\d+) is a (.*) event type$", (Integer index, String eventType) -> {
            EventOutput[] events = testContext.getResponseBody(EventOutput[].class);
            assertThat(events[index], hasProperty("type", endsWith(eventType)));
        });
    }

    public ResponseEntity getModuleEvents(ModuleIO moduleInput, Class responseType) {
        return restTemplate.getForEntity("/events/modules/{name}/{version}/{type}",
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                moduleInput.getIsWorkingCopy() ? "workingcopy" : "release");
    }

    public ResponseEntity getPlatformEvents(PlatformIO platformInput, Class responseType) {
        return restTemplate.getForEntity("/events/platforms/{name}/{version}",
                responseType,
                platformInput.getApplicationName(),
                platformInput.getPlatformName());
    }
}
