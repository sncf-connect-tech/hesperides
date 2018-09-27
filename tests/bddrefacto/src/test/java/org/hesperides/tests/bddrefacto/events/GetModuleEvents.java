package org.hesperides.tests.bddrefacto.events;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.events.EventOutput;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasProperty;
import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertOK;
import static org.hesperides.tests.bddrefacto.commons.StepHelper.getResponseType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class GetModuleEvents implements En {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ModuleBuilder moduleBuilder;

    private ResponseEntity responseEntity;

    public GetModuleEvents() {

        When("^I( try to)? get the events of this module$", (final String tryTo) -> {
            responseEntity = getModuleEvents(moduleBuilder.build(), getResponseType(tryTo, EventOutput[].class));
        });

        Then("^(\\d+) event(?: is|s are) returned$", (final Integer nbEvents) -> {
            assertOK(responseEntity);
            EventOutput[] events = (EventOutput[]) responseEntity.getBody();
            assertEquals(nbEvents.intValue(), events.length);
        });

        Then("^event at index (\\d+) is a (.*) event type$", (final Integer index, final String eventType) -> {
            EventOutput[] events = (EventOutput[]) responseEntity.getBody();
            assertThat(events[index], hasProperty("type", endsWith(eventType)));
        });
    }

    public ResponseEntity getModuleEvents(ModuleIO moduleInput, Class responseType) {
        return restTemplate.getForEntity("/events/modules/{name}/{version}/{type}",
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                moduleInput.isWorkingCopy() ? "workingcopy" : "release");
    }
}
