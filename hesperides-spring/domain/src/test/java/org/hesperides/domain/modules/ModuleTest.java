package org.hesperides.domain.modules;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class ModuleTest {

    private FixtureConfiguration<Module> fixture;

    @BeforeEach
    void setUp() throws Exception {
        fixture = new AggregateTestFixture<>(Module.class);
    }

    @Test
    void should_have_module_created_event_when_create_module_command_apply() {

        String id = "module_test";
        fixture.given()
                .when(new CreateModuleCommand(id))
                .expectEvents(new ModuleCreatedEvent(id));

    }

    @Test
    void given_an_existing_module_release_command_should_change_version() {
        String id = "module_test";

        fixture.given(new ModuleCreatedEvent(id))
                .when(new ReleaseModuleCommand(id,"123"))
                .expectEvents(new ModuleReleasedEvent(id, "123"))
        ;
    }

}