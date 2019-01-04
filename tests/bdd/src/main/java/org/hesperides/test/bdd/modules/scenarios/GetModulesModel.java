package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class GetModulesModel extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private PropertyBuilder propertyBuilder;
    @Autowired
    private ModelBuilder modelBuilder;

    public GetModulesModel() {

        Given("^an existing module with iterable properties$", () -> {
            moduleClient.create(moduleBuilder.build());

            propertyBuilder.reset().withName("module-foo").withProperty(new PropertyBuilder().withName("module-bar"));
            modelBuilder.withIterableProperty(propertyBuilder.build());
            templateBuilder.withContent(propertyBuilder.toString());

            moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
        });

        Given("^an existing module with iterable-ception$", () -> {
            moduleClient.create(moduleBuilder.build());

            propertyBuilder.reset().withName("module-foo").withProperty(new PropertyBuilder().withName("module-bar").withProperty(new PropertyBuilder().withName("module-foobar")));
            modelBuilder.withIterableProperty(propertyBuilder.build());
            templateBuilder.withContent(propertyBuilder.toString());

            moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
        });

        Given("^an existing module with properties with the same name and comment but different default values in multiple templates$", () -> {
            moduleClient.create(moduleBuilder.build());

            propertyBuilder.reset().withName("foo").withComment("comment").withDefaultValue("b");
            modelBuilder.withProperty(propertyBuilder.build());
            templateBuilder.reset().withName("template-b").withContent(propertyBuilder.toString());
            moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());

            propertyBuilder.reset().withName("foo").withComment("comment").withDefaultValue("c");
            templateBuilder.reset().withName("template-c").withContent(propertyBuilder.toString());
            moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());

            propertyBuilder.reset().withName("foo").withComment("comment").withDefaultValue("a-1");
            TemplateBuilder templateA = templateBuilder.reset().withName("template-a").withContent(propertyBuilder.toString());
            propertyBuilder.reset().withName("foo").withComment("comment").withDefaultValue("a-2");
            templateA.withContent(propertyBuilder.toString());
            moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());

            propertyBuilder.reset().withName("foo").withComment("comment").withDefaultValue("d");
            templateBuilder.reset().withName("template-d").withContent(propertyBuilder.toString());
            moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
        });

        Given("^an existing module with properties with the same name but different comments in two templates$", () -> {
            moduleClient.create(moduleBuilder.build());

            propertyBuilder.reset().withName("foo").withComment("comment-a");
            modelBuilder.withProperty(propertyBuilder.build());
            templateBuilder.reset().withName("template-a").withContent(propertyBuilder.toString());
            moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());

            propertyBuilder.reset().withName("foo").withComment("comment-b");
            modelBuilder.withProperty(propertyBuilder.build());
            templateBuilder.reset().withName("template-b").withContent(propertyBuilder.toString());
            moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
        });

        Given("^the module template properties are modified$", () -> {
            templateBuilder.reset().withVersionId(1);
            modelBuilder.reset();

            propertyBuilder.reset().withName("pomme");
            modelBuilder.withProperty(propertyBuilder.build());
            templateBuilder.withContent(propertyBuilder.toString());

            propertyBuilder.reset().withName("banane");
            modelBuilder.withProperty(propertyBuilder.build());
            templateBuilder.withContent(propertyBuilder.toString());

            moduleClient.updateTemplate(templateBuilder.build(), moduleBuilder.build(), TemplateIO.class);
        });

        When("^I( try to)? get the model of this module$", (String tryTo) -> {
            testContext.responseEntity = moduleClient.getModel(moduleBuilder.build(), getResponseType(tryTo, ModelOutput.class));
        });

        Then("^the model of this module contains the(?: updated)? properties$", () -> {
            assertOK();
            ModelOutput expectedModel = modelBuilder.build();
            ModelOutput actualModel = (ModelOutput) testContext.getResponseBody();
            assertEquals(expectedModel, actualModel);
        });

        Then("^the model of this module contains the property with the same name and comment$", () -> {
            assertOK();
            PropertyOutput expectedProperty = getFirstProperty(modelBuilder.build());
            PropertyOutput actualProperty = getFirstProperty((ModelOutput) testContext.getResponseBody());
            assertEquals(expectedProperty.getName(), actualProperty.getName());
            assertEquals(expectedProperty.getComment(), actualProperty.getComment());
            assertEquals(expectedProperty.getDefaultValue(), actualProperty.getDefaultValue());
        });

        Then("^the module model if not found$", () -> {
            assertNotFound();
        });

        Then("^the model of this module doesn't contain the properties$", () -> {
            assertOK();
            ModelOutput expectedModel = new ModelBuilder().build();
            ModelOutput actualModel = (ModelOutput) testContext.getResponseBody();
            assertEquals(expectedModel, actualModel);
        });
    }

    private PropertyOutput getFirstProperty(ModelOutput modelOutput) {
        return new ArrayList<>(modelOutput.getProperties()).get(0);
    }
}
