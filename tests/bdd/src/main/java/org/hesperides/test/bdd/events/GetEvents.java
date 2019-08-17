package org.hesperides.test.bdd.events;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.events.EventOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class GetEvents extends HesperidesScenario implements En {

    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private PlatformBuilder platformBuilder;

    public GetEvents() {

        When("^I( try to)? get the events of this module$", (String tryTo) -> {
            getModuleEvents(moduleBuilder.build(), tryTo);
        });

        When("^I( try to)? get the events of this platform$", (String tryTo) -> {
            getPlatformEvents(platformBuilder.buildInput(), tryTo);
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

    // TODO EventClient ?
    private void getModuleEvents(ModuleIO moduleInput, String tryTo) {
        restTemplate.getForEntity("/events/modules/{name}/{version}/{type}",
                getResponseType(tryTo, EventOutput[].class),
                moduleInput.getName(),
                moduleInput.getVersion(),
                VersionType.fromIsWorkingCopy(moduleInput.getIsWorkingCopy()));
    }

    private void getPlatformEvents(PlatformIO platformInput, String tryTo) {
        restTemplate.getForEntity("/events/platforms/{name}/{version}",
                getResponseType(tryTo, EventOutput[].class),
                platformInput.getApplicationName(),
                platformInput.getPlatformName());
    }
}
