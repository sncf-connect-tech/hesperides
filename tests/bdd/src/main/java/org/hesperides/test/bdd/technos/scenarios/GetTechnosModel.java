package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class GetTechnosModel extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private PropertyBuilder propertyBuilder;
    @Autowired
    private ModelBuilder modelBuilder;

    public GetTechnosModel() {

        Given("^an existing techno with iterable properties$", () -> {
            propertyBuilder.reset().withName("techno-foo").withProperty(new PropertyBuilder().withName("techno-bar"));
            modelBuilder.withIterableProperty(propertyBuilder.build());
            templateBuilder.withContent(propertyBuilder.toString());
            technoClient.addTemplate(templateBuilder.build(), technoBuilder.build());
        });

        Given("^an existing techno with iterable-ception$", () -> {
            propertyBuilder.reset().withName("techno-foo").withProperty(new PropertyBuilder().withName("techno-bar").withProperty(new PropertyBuilder().withName("techno-foobar")));
            modelBuilder.withIterableProperty(propertyBuilder.build());
            templateBuilder.withContent(propertyBuilder.toString());

            technoClient.addTemplate(templateBuilder.build(), technoBuilder.build());
        });

        Given("^an existing techno with properties with the same name and comment, but different default values, in two templates$", () -> {
            // On peut définir le model attendu ici ou à partir de la seconde propriété,
            // cela ne change rien puisque le assertEquals tient compte du equals de PropertyOutput
            // qui ne tient compte que du nom et du commentaire de la propriété.
            // Si on veut tester quelle valeur par défaut est prise en compte,
            // il faut créer une classe PropertyAssert qui tester l'égalité de tous les champs.
            propertyBuilder.reset().withName("foo").withComment("comment").withDefaultValue("12");
            modelBuilder.withProperty(propertyBuilder.build());
            templateBuilder.reset().withName("template-a").withContent(propertyBuilder.toString());
            technoClient.addTemplate(templateBuilder.build(), technoBuilder.build());

            propertyBuilder.reset().withName("foo").withComment("comment").withDefaultValue("42");
            templateBuilder.reset().withName("template-b").withContent(propertyBuilder.toString());
            technoClient.addTemplate(templateBuilder.build(), technoBuilder.build());
        });

        Given("^an existing techno with properties with the same name but different comments in two templates$", () -> {
            propertyBuilder.reset().withName("foo").withComment("comment-a");
            modelBuilder.withProperty(propertyBuilder.build());
            templateBuilder.reset().withName("template-a").withContent(propertyBuilder.toString());
            technoClient.addTemplate(templateBuilder.build(), technoBuilder.build());

            propertyBuilder.reset().withName("foo").withComment("comment-b");
            modelBuilder.withProperty(propertyBuilder.build());
            templateBuilder.reset().withName("template-b").withContent(propertyBuilder.toString());
            technoClient.addTemplate(templateBuilder.build(), technoBuilder.build());
        });

        Given("^the techno template properties are modified$", () -> {
            templateBuilder.reset().withVersionId(1);
            modelBuilder.reset();

            propertyBuilder.reset().withName("pomme");
            modelBuilder.withProperty(propertyBuilder.build());
            templateBuilder.withContent(propertyBuilder.toString());

            propertyBuilder.reset().withName("banane");
            modelBuilder.withProperty(propertyBuilder.build());
            templateBuilder.withContent(propertyBuilder.toString());

            technoClient.updateTemplate(templateBuilder.build(), technoBuilder.build());
        });

        When("^I( try to)? get the model of this techno$", (String tryTo) -> {
            testContext.setResponseEntity(technoClient.getModel(technoBuilder.build(), getResponseType(tryTo, ModelOutput.class)));
        });

        Then("^the model of this techno contains the properties$", () -> {
            assertOK();
            ModelOutput expectedModel = modelBuilder.build();
            ModelOutput actualModel = testContext.getResponseBody(ModelOutput.class);
            assertEquals(expectedModel, actualModel);
        });

        Then("^the techno model is empty$", () -> {
            ModelOutput expectedModel = new ModelOutput(Collections.emptySet(), Collections.emptySet());
            assertEquals(expectedModel, testContext.getResponseBody(ModelOutput.class));
        });

        Then("^the model of this techno doesn't contain the properties$", () -> {
            assertOK();
            ModelOutput expectedModel = new ModelBuilder().build();
            ModelOutput actualModel = testContext.getResponseBody(ModelOutput.class);
            assertEquals(expectedModel, actualModel);
        });
    }
}
