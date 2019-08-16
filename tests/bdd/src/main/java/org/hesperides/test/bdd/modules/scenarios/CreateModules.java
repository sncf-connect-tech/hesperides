package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.templatecontainers.VersionType;
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
                "( (?:and|with) (?:a|this) template(?: with a \"/\" in the title)?)?" +
                "( (?:and|with) properties)?" +
                "( (?:and|with) password properties)?" +
                "( (?:and|with) global properties)?" +
                "( (?:and|with) iterable properties)?" +
                "( (?:and|with) nested iterable properties)?" +
                "( (?:and|with) this techno)?$", (
                String released,
                String moduleName,
                String withTemplate, //TODO à revoir
                String withProperties,
                String withPasswordProperties,
                String withGlobalProperties,
                String withIterableProperties,
                String withNestedIterableProperties,
                String withThisTechno) -> {

            if (isNotEmpty(moduleName)) {
                moduleBuilder.withName(moduleName);
            }


            if (isNotEmpty(withThisTechno)) {
                moduleBuilder.withTechnoBuilder(technoBuilder);
            }

            createModule();

            //TODO à revoir
            if (isEmpty(withTemplate) || !withTemplate.contains("this")) {
                templateBuilder.reset();
            }
            if (isNotEmpty(withTemplate) && withTemplate.contains("\"/\" in the title")) { // Est-ce que c'est utilisé ?
                templateBuilder.withName("a/template");
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

            if (isNotEmpty(withTemplate) ||
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

        Given("^a list of ?(\\d+)? modules( with different names)?(?: with the same name)?$", (String modulesCount, String withDifferentNames) -> {
            int modulesToCreateCount = isEmpty(modulesCount) ? 12 : Integer.parseInt(modulesCount);
            IntStream.range(0, modulesToCreateCount).forEach(index -> {
                if (isNotEmpty(withDifferentNames)) {
                    moduleBuilder.withName("a-module-" + index);
                } else {
                    moduleBuilder.withVersion("0." + (index + 1));
                }
                createModule();
            });
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
        moduleClient.releaseModule(moduleBuilder.build());
        assertOK();
        moduleBuilder.withVersionType(VersionType.RELEASE);
        moduleHistory.addModuleBuilder(moduleBuilder);
    }
}
