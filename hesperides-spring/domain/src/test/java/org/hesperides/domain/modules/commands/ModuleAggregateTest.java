package org.hesperides.domain.modules.commands;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.events.ModuleCopiedEvent;
import org.hesperides.domain.modules.events.ModuleCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Exemple de test unitaire sur les commandes
 */
class ModuleAggregateTest {

    private FixtureConfiguration<ModuleAggregate> fixture;

    @BeforeEach
    void setUp() throws Exception {
        fixture = new AggregateTestFixture<>(ModuleAggregate.class);
    }

    @Test
    void when_create_module_command_then_expect_module_created() {

        Module.Key id = new Module.Key("module_test", "123", Module.Type.workingcopy);
        fixture.given()
                .when(new CreateModuleCommand(id))
                .expectEvents(new ModuleCreatedEvent(id));

    }

    @Test
    void when_copy_module_command_then_expect_module_created_from_another_module() {

        Module.Key id = new Module.Key("module_test", "123", Module.Type.workingcopy);
        Module.Key source = new Module.Key("module_test", "1234", Module.Type.workingcopy);
        fixture.given()
                .when(new CopyModuleCommand(id, source))
                .expectEvents(new ModuleCopiedEvent(id, source));
    }

//
//    @Test
//    void given_an_existing_module_release_command_should_change_version() {
//        ModuleAggregate.Key id = new ModuleAggregate.Key("module_test","123");
//
//        fixture.given(new ModuleCreatedEvent(id))
//                .when(new ReleaseModuleCommand(id,"123"))
//                .expectEvents(new ModuleReleasedEvent(id, "123"))
//        ;
//    }

}