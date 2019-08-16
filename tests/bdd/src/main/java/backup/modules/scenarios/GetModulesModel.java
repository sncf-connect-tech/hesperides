package backup.modules.scenarios;

import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.hesperides.test.bdd.modules.OldModuleClient;
import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class GetModulesModel extends HesperidesScenario implements En {

    @Autowired
    private OldModuleClient moduleClient;
    @Autowired
    private OldModuleBuilder moduleBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private PropertyBuilder propertyBuilder;
    @Autowired
    private ModelBuilder modelBuilder;

    public GetModulesModel() {

        Given("^an existing module with iterable properties$", () -> {
            testContext.setResponseEntity(moduleClient.create(moduleBuilder.build()));
            assertCreated();

            propertyBuilder.reset().withName("module-foo").withProperty(new PropertyBuilder().withName("module-bar"));
            modelBuilder.withIterableProperty(propertyBuilder.build());
            templateBuilder.withContent(propertyBuilder.toString());

            moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
        });

        Given("^an existing module with iterable-ception$", () -> {
            testContext.setResponseEntity(moduleClient.create(moduleBuilder.build()));
            assertCreated();

            propertyBuilder.reset().withName("module-foo").withProperty(new PropertyBuilder().withName("module-bar").withProperty(new PropertyBuilder().withName("module-foobar")));
            modelBuilder.withIterableProperty(propertyBuilder.build());
            templateBuilder.withContent(propertyBuilder.toString());

            moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
        });

        Given("^an existing module with properties with the same name and comment but different default values in multiple templates$", () -> {
            testContext.setResponseEntity(moduleClient.create(moduleBuilder.build()));
            assertCreated();

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
            testContext.setResponseEntity(moduleClient.create(moduleBuilder.build()));
            assertCreated();

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
            testContext.setResponseEntity(moduleClient.getModel(moduleBuilder.build(), getResponseType(tryTo, ModelOutput.class)));
        });

        Then("^the model of this module contains the(?: updated)? properties$", () -> {
            assertOK();
            ModelOutput expectedModel = modelBuilder.build();
            ModelOutput actualModel = testContext.getResponseBody(ModelOutput.class);
            assertEquals(expectedModel, actualModel);
        });

        Then("^the model of this module contains the property with the same name and comment$", () -> {
            assertOK();
            PropertyOutput expectedProperty = getFirstProperty(modelBuilder.build());
            PropertyOutput actualProperty = getFirstProperty(testContext.getResponseBody(ModelOutput.class));
            assertEquals(expectedProperty.getName(), actualProperty.getName());
            assertEquals(expectedProperty.getComment(), actualProperty.getComment());
            assertEquals(expectedProperty.getDefaultValue(), actualProperty.getDefaultValue());
        });

        Then("^the model of this module lists (\\d+) propert(?:y|ies)$", (Integer propertyCount) -> {
            assertOK();
            Set<PropertyOutput> actualProperties = testContext.getResponseBody(ModelOutput.class).getProperties();
            assertThat(actualProperties, hasSize(propertyCount));
        });

        Then("^the model of property \"([^\"]+)\"" +
                        "( is a required password)?" +
                        "(?: has a comment of \"([^\"]+)\")?" +
                        "(?: and(?: has)? a default value of \"([^\"]+)\")?",
                (String propertyName, String isRequiredPassword, String comment, String defaultValue) -> {
                    assertOK();
                    Set<PropertyOutput> actualProperties = testContext.getResponseBody(ModelOutput.class).getProperties();
                    PropertyOutput matchingPropertyModel = actualProperties.stream().filter(p -> p.getName().equals(propertyName)).findAny().orElse(null);
                    assertNotNull(matchingPropertyModel);
                    if (isNotBlank(isRequiredPassword)) {
                        assertTrue(matchingPropertyModel.isRequired());
                        assertTrue(matchingPropertyModel.isPassword());
                    }
                    if (isNotBlank(comment)) {
                        assertEquals(comment, matchingPropertyModel.getComment());
                    }
                    if (isNotBlank(defaultValue)) {
                        assertEquals(matchingPropertyModel.getDefaultValue(), defaultValue);
                    }
                });

        Then("^the module model if not found$", () -> {
            assertNotFound();
        });

        Then("^the model of this module doesn't contain the properties$", () -> {
            assertOK();
            ModelOutput expectedModel = new ModelBuilder().build();
            ModelOutput actualModel = testContext.getResponseBody(ModelOutput.class);
            assertEquals(expectedModel, actualModel);
        });
    }

    private PropertyOutput getFirstProperty(ModelOutput modelOutput) {
        return new ArrayList<>(modelOutput.getProperties()).get(0);
    }
}
