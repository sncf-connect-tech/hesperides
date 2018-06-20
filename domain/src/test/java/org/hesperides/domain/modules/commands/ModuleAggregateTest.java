package org.hesperides.domain.modules.commands;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.exceptions.DuplicateTemplateCreationException;
import org.hesperides.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainers.entities.Template;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Exemple de test unitaire sur les commandes
 */
class ModuleAggregateTest {

    private FixtureConfiguration<ModuleAggregate> fixture;

    private TemplateContainer.Key moduleKey = new Module.Key("module_test", "123", TemplateContainer.VersionType.workingcopy);
    private Module module = new Module(moduleKey, new ArrayList<>(), Collections.emptyList(), 1L);
    private Template.Rights rights = new Template.Rights(null, null, null);
    private Template template = new Template("template1", "file1.txt", "/", "content", rights, 1L, moduleKey);
    private User user = new User("default_name", true, true);

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
                .when(new CreateTemplateCommand(moduleKey, template, user))
                .expectEvents(new TemplateCreatedEvent(moduleKey, template, user));
    }

    @Test
    void when_create_template_already_existing_then_expect_error() {
        fixture.given(new ModuleCreatedEvent(module, user))
                .andGiven(new TemplateCreatedEvent(moduleKey, template, user))
                .when(new CreateTemplateCommand(moduleKey, template, user))
                .expectException(DuplicateTemplateCreationException.class);
    }

    @Test
    void when_update_template_expect_template_updated_event() {
        Template updatedTemplate = new Template(template.getName(), template.getFilename(), template.getLocation(), template.getContent(), template.getRights(), template.getVersionId() + 1, template.getTemplateContainerKey());
        fixture.given(new ModuleCreatedEvent(module, user))
                .andGiven(new TemplateCreatedEvent(moduleKey, template, user))
                .when(new UpdateTemplateCommand(moduleKey, template, user))
                .expectEvents(new TemplateUpdatedEvent(moduleKey, updatedTemplate, user));
    }

    @Test
    void when_update_template_that_do_not_exist_expect_error() {
        fixture.given(new ModuleCreatedEvent(module, user))
                .when(new UpdateTemplateCommand(moduleKey, template, user))
                .expectException(TemplateNotFoundException.class);
    }

    @Test
    void when_delete_template_that_do_not_exist_expect_nothing() {
        fixture.given(new ModuleCreatedEvent(module, user))
                .when(new DeleteTemplateCommand(moduleKey, template.getName(), user))
                .expectNoEvents();
    }

    @Test
    void when_delete_template_expect_template_deleted_event() {
        fixture.given(new ModuleCreatedEvent(module, user))
                .andGiven(new TemplateCreatedEvent(moduleKey, template, user))
                .when(new DeleteTemplateCommand(moduleKey, template.getName(), user))
                .expectEvents(new TemplateDeletedEvent(moduleKey, template.getName(), user));
    }


//    @Test
//    void when_copy_module_command_then_expect_module_created_from_another_module() {
//
//        TemplateContainer.Key moduleKey = new TemplateContainer.Key("module_test", "123", Module.VersionType.workingcopy);
//        TemplateContainer.Key source = new TemplateContainer.Key("module_test", "1234", Module.VersionType.workingcopy);
//        fixture.given()
//                .when(new CopyModuleCommand(moduleKey, source))
//                .expectEvents(new ModuleCopiedEvent(moduleKey, source));
//    }

//
//    @Test
//    void given_an_existing_module_release_command_should_change_version() {
//        ModuleAggregate.Key moduleKey = new ModuleAggregate.Key("module_test","123");
//
//        fixture.given(new ModuleCreatedEvent(moduleKey))
//                .when(new ReleaseModuleCommand(moduleKey,"123"))
//                .expectEvents(new ModuleReleasedEvent(moduleKey, "123"))
//        ;
//    }

}