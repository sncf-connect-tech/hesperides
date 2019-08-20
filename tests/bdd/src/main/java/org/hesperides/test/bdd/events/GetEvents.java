package org.hesperides.test.bdd.events;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.events.EventOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformBuilder;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


public class GetEvents extends HesperidesScenario implements En {

    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private OldPlatformBuilder oldPlatformBuilder;

    public GetEvents() {

        When("^I( try to)? get the events of this module$", (String tryTo) -> {
            getModuleEvents(moduleBuilder.build(), tryTo);
        });

        When("^I( try to)? get the events of this platform$", (String tryTo) -> {
            getPlatformEvents(oldPlatformBuilder.buildInput(), tryTo);
        });

        Then("^(\\d+) event(?: is|s are) returned$", (Integer nbEvents) -> {
            assertOK();
            List<EventOutput> events = testContext.getResponseBodyAsList();
            assertThat(events, hasSize(nbEvents));
        });

        Then("^event at index (\\d+) is a (.*) event type$", (Integer index, String eventType) -> {
            List<EventOutput> events = testContext.getResponseBody();
            assertThat(events.get(index), hasProperty("type", endsWith(eventType)));
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
