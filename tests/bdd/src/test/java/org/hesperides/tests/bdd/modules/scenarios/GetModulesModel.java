package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.ModuleClient;
import org.hesperides.tests.bdd.templatecontainers.builders.ModelBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bdd.commons.HesperidesScenario.*;
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

        Given("^an existing module with properties with the same name and comment, but different default values, in two templates$", () -> {
            moduleClient.create(moduleBuilder.build());

            // On peut définir le model attendu ici ou à partir de la seconde propriété,
            // cela ne change rien puisque le assertEquals tient compte du equals de PropertyOutput
            // qui ne tient compte que du nom et du commentaire de la propriété.
            // Si on veut tester quelle valeur par défaut est prise en compte,
            // il faut créer une classe PropertyAssert qui tester l'égalité de tous les champs.
            propertyBuilder.reset().withName("foo").withComment("comment").withDefaultValue("12");
            modelBuilder.withProperty(propertyBuilder.build());
            templateBuilder.reset().withName("template-a").withContent(propertyBuilder.toString());
            moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());

            propertyBuilder.reset().withName("foo").withComment("comment").withDefaultValue("42");
            templateBuilder.reset().withName("template-b").withContent(propertyBuilder.toString());
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
}
