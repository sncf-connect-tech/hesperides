package org.hesperides.tests.bdd.events;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.events.EventOutput;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class GetModuleEvents extends HesperidesScenario implements En {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetModuleEvents() {

        When("^I( try to)? get the events of this module$", (String tryTo) -> {
            testContext.responseEntity = getModuleEvents(moduleBuilder.build(), getResponseType(tryTo, EventOutput[].class));
        });

        Then("^(\\d+) event(?: is|s are) returned$", (Integer nbEvents) -> {
            assertOK();
            EventOutput[] events = (EventOutput[]) testContext.getResponseBody();
            assertEquals(nbEvents.intValue(), events.length);
        });

        Then("^event at index (\\d+) is a (.*) event type$", (Integer index, String eventType) -> {
            EventOutput[] events = (EventOutput[]) testContext.getResponseBody();
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
}
