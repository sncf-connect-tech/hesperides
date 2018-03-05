package org.hesperides.domain.modules.commands;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.hesperides.domain.modules.exceptions.DuplicateTemplateCreationException;
import org.hesperides.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.domain.security.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * Exemple de test unitaire sur les commandes
 */
class ModuleAggregateTest {

    private FixtureConfiguration<ModuleAggregate> fixture;

    private Module.Key id = new Module.Key("module_test", "123", Module.Type.workingcopy);
    private Module module = new Module(id, new ArrayList<>(), 1L);
    private Template.Rights rights = new Template.Rights();
    private Template template = new Template("template1", "file1.txt", "/", "content", rights, id);
    private User user = new User("default_name");

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(ModuleAggregate.class);
    }

    @Test
    void when_create_module_command_then_expect_module_created() {
        fixture.given()
                .when(new CreateModuleCommand(module, user))
                .expectEvents(new ModuleCreatedEvent(module, user));
    }

    @Test
    void when_create_template_then_expect_template_created() {
        fixture.given(new ModuleCreatedEvent(module, user))
                .when(new CreateTemplateCommand(id, template, user))
                .expectEvents(new TemplateCreatedEvent(id, template, user));
    }

    @Test
    void when_create_template_already_existing_then_expect_error() {
        fixture.given(new ModuleCreatedEvent(module, user))
                .andGiven(new TemplateCreatedEvent(id, template, user))
                .when(new CreateTemplateCommand(id, template, user))
                .expectException(DuplicateTemplateCreationException.class);
    }

    @Test
    void when_update_template_expect_template_updated_event() {
        fixture.given(new ModuleCreatedEvent(module, user))
                .andGiven(new TemplateCreatedEvent(id, template, user))
                .when(new UpdateTemplateCommand(id, template, user))
                .expectEvents(new TemplateUpdatedEvent(id, template, user));
    }

    @Test
    void when_update_template_that_do_not_exist_expect_error() {
        fixture.given(new ModuleCreatedEvent(module, user))
                .when(new UpdateTemplateCommand(id, template, user))
                .expectException(TemplateNotFoundException.class);
    }

    @Test
    void when_delete_template_that_do_not_exist_expect_nothing() {
        fixture.given(new ModuleCreatedEvent(module, user))
                .when(new DeleteTemplateCommand(id, template.getName(), user))
                .expectNoEvents();
    }

    @Test
    void when_delete_template_expect_template_deleted_event() {
        fixture.given(new ModuleCreatedEvent(module, user))
                .andGiven(new TemplateCreatedEvent(id, template, user))
                .when(new DeleteTemplateCommand(id, template.getName(), user))
                .expectEvents(new TemplateDeletedEvent(id, template.getName(), user));
    }


//    @Test
//    void when_copy_module_command_then_expect_module_created_from_another_module() {
//
//        Module.Key id = new Module.Key("module_test", "123", Module.Type.workingcopy);
//        Module.Key source = new Module.Key("module_test", "1234", Module.Type.workingcopy);
//        fixture.given()
//                .when(new CopyModuleCommand(id, source))
//                .expectEvents(new ModuleCopiedEvent(id, source));
//    }

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