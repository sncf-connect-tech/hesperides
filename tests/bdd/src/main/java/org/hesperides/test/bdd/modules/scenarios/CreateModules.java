package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;

public class CreateModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleHistory moduleHistory;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private PropertyBuilder propertyBuilder;

    public CreateModules() {

        Given("^an existing( released)? module" +
                "(?: named \"([^\"]*)\")?" +
                "( (?:and|with) (?:this|a) template)?" +
                "( (?:and|with) properties)?" +
                "( (?:and|with) password properties)?" +
                "( (?:and|with) global properties)?" +
                "( (?:and|with) iterable properties)?" +
                "( (?:and|with) nested iterable properties)?" +
                "( (?:and|with) this techno)?$", (
                String released,
                String moduleName,
                String withThisTemplate,
                String withProperties,
                String withPasswordProperties,
                String withGlobalProperties,
                String withIterableProperties,
                String withNestedIterableProperties,
                String withThisTechno) -> {

            moduleBuilder.reset();

            if (isNotEmpty(moduleName)) {
                moduleBuilder.withName(moduleName);
            }

            if (isNotEmpty(withThisTechno)) {
                moduleBuilder.withTechnoBuilder(technoBuilder);
            }

            createModule();

            if (isNotEmpty(withThisTemplate)) {
                addTemplatePropertiesToBuilders(templateBuilder);
            }

            if (isNotEmpty(withProperties)) {
                addPropertyToBuilders("module-foo");
                addPropertyToBuilders("module-bar");
            }
            if (isNotEmpty(withPasswordProperties)) {
                propertyBuilder.reset().withName("module-fuzz").withIsPassword();
                addPropertyToBuilders(propertyBuilder);
            }
            if (isNotEmpty(withGlobalProperties)) {
                addPropertyToBuilders("global-module-foo");
                addPropertyToBuilders("global-module-bar");
            }
            if (isNotEmpty(withIterableProperties)) {
                propertyBuilder.reset()
                        .withName("module-foo")
                        .withProperty(new PropertyBuilder()
                                .withName("module-bar"));
                addPropertyToBuilders(propertyBuilder);
            }
            if (isNotEmpty(withIterableProperties)) {
                propertyBuilder.reset()
                        .withName("module-foo")
                        .withProperty(new PropertyBuilder()
                                .withName("module-bar")
                                .withProperty(new PropertyBuilder()
                                        .withName("module-foobar")));
                addPropertyToBuilders(propertyBuilder);
            }

            if (isNotEmpty(withThisTemplate) ||
                    isNotEmpty(withProperties) ||
                    isNotEmpty(withGlobalProperties) ||
                    isNotEmpty(withPasswordProperties) ||
                    isNotEmpty(withIterableProperties) ||
                    isNotEmpty(withNestedIterableProperties)) {
                addTemplateToModule();
            }

            if (isNotEmpty(released)) {
                releaseModule();
            }
        });

        Given("^a module with (\\d+) versions$", (Integer nbVersions) -> {
            moduleBuilder.withName("new-module");
            IntStream.range(0, nbVersions).forEach(index -> {
                moduleBuilder.withVersion("1." + index);
                createModule();
            });
        });

        Given("^a list of( \\d+)? modules( with different names(?: starting with the same prefix)?)?(?: with the same name)?$", (String modulesCount, String withDifferentNames) -> {
            int modulesToCreateCount = isEmpty(modulesCount) ? 12 : Integer.parseInt(modulesCount.substring(1));
            for (int i = 0; i < modulesToCreateCount; i++) {
                boolean isLast = i == (modulesToCreateCount - 1);
                // Note: il faut créer en dernier le module associé à un "match exact",
                // car par défaut Mongo semble remonter les résultats ordonnés par date de création
                if (isLast || isEmpty(withDifferentNames)) {
                    moduleBuilder.withName("new-module");
                } else {
                    moduleBuilder.withName("new-module-" + i);
                }
                if (isLast) {
                    moduleBuilder.withVersion("0.0.1");
                } else {
                    moduleBuilder.withVersion("0.0.1" + i);
                }
                createModule();
                assertCreated();
            }
        });

        Given("^an existing module with properties with the same name and comment, but different default values, in two templates$", () -> {
            createModule();

            templateBuilder.reset().withName("template-a").withNamespace(moduleBuilder.buildNamespace());
            propertyBuilder.reset().withName("foo").withComment("comment").withDefaultValue("12");
            addPropertyToBuilders(propertyBuilder);
            addTemplateToModule();

            templateBuilder.reset().withName("template-b").withNamespace(moduleBuilder.buildNamespace());
            propertyBuilder.reset().withName("foo").withComment("comment").withDefaultValue("42");
            addPropertyToBuilders(propertyBuilder);
            addTemplateToModule();
        });

        Given("^an existing module with properties with the same name but different comments in two templates$", () -> {
            createModule();

            templateBuilder.reset().withName("template-a").withNamespace(moduleBuilder.buildNamespace());
            propertyBuilder.reset().withName("foo").withComment("comment-a");
            addPropertyToBuilders(propertyBuilder);
            addTemplateToModule();

            templateBuilder.reset().withName("template-b").withNamespace(moduleBuilder.buildNamespace());
            propertyBuilder.reset().withName("foo").withComment("comment-b");
            addPropertyToBuilders(propertyBuilder);
            addTemplateToModule();
        });

        Given("^the module template properties are modified$", () -> {
            addPropertyToBuilders("patate");
            moduleClient.updateTemplate(templateBuilder.build(), moduleBuilder.build());
            assertOK();
            moduleBuilder.updateTemplateBuilder(templateBuilder);
        });

        Given("^a module to create" +
                "( with this techno)?" +
                "(?: with the same name and version)?" +
                "( but different letter case)?" +
                "( without a version type)?$", (
                String withThisTechno,
                String withDifferentLetterCase,
                String withoutVersionType) -> {
            moduleBuilder.reset();
            if (isNotEmpty(withDifferentLetterCase)) {
                moduleBuilder.withName(moduleBuilder.getName().toUpperCase());
            }
            if (isNotEmpty(withThisTechno)) {
                moduleBuilder.withTechnoBuilder(technoBuilder);
            }
            if (isNotEmpty(withoutVersionType)) {
                moduleBuilder.withVersionType(null);
            }
        });

        When("^I( try to)? create this module$", (String tryTo) -> createModule(tryTo));

        Then("^the module is successfully created$", () -> {
            assertCreated();
            ModuleIO expectedModule = moduleBuilder.build();
            ModuleIO actualModule = testContext.getResponseBody(ModuleIO.class);
            assertEquals(expectedModule, actualModule);
        });

        Then("^the module creation is rejected with a conflict error$", this::assertConflict);

        Then("^the module creation is rejected with a not found error$", this::assertNotFound);

        Then("^the module creation is rejected with a bad request error$", this::assertBadRequest);
    }

    private void addTemplatePropertiesToBuilders(TemplateBuilder templateBuilder) {
        extractPropertyToBuilders(templateBuilder.getFilename());
        extractPropertyToBuilders(templateBuilder.getLocation());
        extractPropertyToBuilders(templateBuilder.getContent());
    }

    private void extractPropertyToBuilders(String input) {
        propertyBuilder.extractProperties(input).forEach(this::addPropertyToBuilders);
    }

    private void addPropertyToBuilders(String name) {
        propertyBuilder.reset().withName(name);
        addPropertyToBuilders(propertyBuilder);
    }

    private void addPropertyToBuilders(PropertyBuilder propertyBuilder) {
        templateBuilder.withContent(propertyBuilder.toString());
        moduleBuilder.addPropertyBuilder(propertyBuilder);
    }

    private void createModule() {
        createModule(null);
        assertCreated();
    }

    private void createModule(String tryTo) {
        moduleClient.createModule(moduleBuilder.build(), tryTo);
        moduleBuilder.incrementVersionId();
        moduleHistory.addModuleBuilder(moduleBuilder);
    }

    private void addTemplateToModule() {
        templateBuilder
                .withVersionId(0)
                .withNamespace(moduleBuilder.buildNamespace());
        moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
        assertCreated();
        moduleBuilder.addTemplateBuilder(templateBuilder);
        moduleHistory.updateModuleBuilder(moduleBuilder);
    }

    private void releaseModule() {
        ReleaseModules.releaseModule(moduleClient, moduleBuilder, moduleHistory, null, null);
        assertOK();
    }
}
