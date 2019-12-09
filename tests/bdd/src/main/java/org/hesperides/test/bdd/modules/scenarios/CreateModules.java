package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java.en.Given;
import cucumber.api.java8.En;

import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.hesperides.test.bdd.users.UserAuthorities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
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
    @Autowired
    private ReleaseModules releaseModules;
    @Autowired
    private UserAuthorities userAuthorities;

    private List<CompletableFuture<ResponseEntity>> concurrentCreations;

    @Given("^an existing( released)? module" +
            "(?: named \"([^\"]*)\")?" +
            "(?: with version \"([^\"]*)\")?" +
            "( (?:and|with) (?:this|a) template)?" +
            "( (?:and|with) properties)?" +
            "( (?:and|with) password properties)?" +
            "( (?:and|with) global properties)?" +
            "( (?:and|with) iterable properties(?: referencing global properties)?)?" +
            "( (?:and|with) nested iterable properties)?" +
            "( (?:and|with) this techno)?$")
    public void givenAnExistingModule(
            String released,
            String moduleName,
            String moduleVersion,
            String withThisTemplate,
            String withProperties,
            String withPasswordProperties,
            String withGlobalProperties,
            String withIterableProperties,
            String withNestedIterableProperties,
            String withThisTechno) {

        moduleBuilder.reset();

        if (isNotEmpty(moduleName)) {
            moduleBuilder.withName(moduleName);
        }

        if (isNotEmpty(moduleVersion)) {
            moduleBuilder.withVersion(moduleVersion);
        }

        if (isNotEmpty(withThisTechno)) {
            moduleBuilder.withTechnoBuilder(technoBuilder);
        }

        createModule();

        if (isNotEmpty(withThisTemplate)) {
            addTemplatePropertiesToModuleBuilder(templateBuilder);
        }

        if (isNotEmpty(withProperties)) {
            addPropertyToTemplateContentAndModuleBuilder("module-foo");
            addPropertyToTemplateContentAndModuleBuilder("module-bar");
        }
        if (isNotEmpty(withPasswordProperties)) {
            propertyBuilder.reset().withName("module-fuzz").withIsPassword();
            addPropertyToTemplateContentAndModuleBuilder(propertyBuilder);
        }
        if (isNotEmpty(withGlobalProperties)) {
            addPropertyToTemplateContentAndModuleBuilder("global-module-foo");
            addPropertyToTemplateContentAndModuleBuilder("global-module-bar");
        }
        if (isNotEmpty(withIterableProperties)) {
            String propertyName = withIterableProperties.contains("global") ? "global-module-foo" : "module-bar";
            propertyBuilder.reset()
                    .withName("module-foo")
                    .withProperty(new PropertyBuilder()
                            .withName(propertyName));
            addPropertyToTemplateContentAndModuleBuilder(propertyBuilder);
        }
        if (isNotEmpty(withNestedIterableProperties)) {
            propertyBuilder.reset()
                    .withName("module-foo")
                    .withProperty(new PropertyBuilder()
                            .withName("module-bar")
                            .withProperty(new PropertyBuilder()
                                    .withName("module-foobar")));
            addPropertyToTemplateContentAndModuleBuilder(propertyBuilder);
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
            releaseModules.releaseModule();
            assertOK();
        }
    }

    public CreateModules() {

        /*
         * Cette étape est la fusion de 2 glues :
         * - {@code Given a template with the following content}
         * - {@code Given a module with this template}
         *
         * il y a 61 tests qui dépendent de cette étape.
         */
        Given("^an existing module(?: named \"([^\"]*)\")?(?: with version \"([^\"]*)\")? with this template content$", (String moduleName, String moduleVersion, String templateContent) -> {
            moduleBuilder.reset();
            if (isNotEmpty(moduleName)) {
                moduleBuilder.withName(moduleName);
            }
            if (isNotEmpty(moduleVersion)) {
                moduleBuilder.withVersion(moduleVersion);
            }
            createModule();
            templateBuilder.setContent(templateContent);
            addTemplatePropertiesToModuleBuilder(templateBuilder);
            addTemplateToModule();
        });

        Given("^a module with (\\d+) versions$", (Integer nbVersions) -> {
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
            addPropertyToTemplateContentAndModuleBuilder(propertyBuilder);
            addTemplateToModule();

            templateBuilder.reset().withName("template-b").withNamespace(moduleBuilder.buildNamespace());
            propertyBuilder.reset().withName("foo").withComment("comment").withDefaultValue("42");
            addPropertyToTemplateContentAndModuleBuilder(propertyBuilder);
            addTemplateToModule();
        });

        Given("^an existing module with properties with the same name but different comments in two templates$", () -> {
            createModule();

            templateBuilder.reset().withName("template-a").withNamespace(moduleBuilder.buildNamespace());
            propertyBuilder.reset().withName("foo").withComment("comment-a");
            addPropertyToTemplateContentAndModuleBuilder(propertyBuilder);
            addTemplateToModule();

            templateBuilder.reset().withName("template-b").withNamespace(moduleBuilder.buildNamespace());
            propertyBuilder.reset().withName("foo").withComment("comment-b");
            addPropertyToTemplateContentAndModuleBuilder(propertyBuilder);
            addTemplateToModule();
        });

        Given("^the module template properties are modified$", () -> {
            addPropertyToTemplateContentAndModuleBuilder("patate");
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

        Given("^a module with a property \"([^\"]+)\" existing in versions: (.+)$", (String propertyName, String versions) -> {
            Arrays.stream(versions.split(", ")).forEach(version -> {
                moduleBuilder.reset();
                addPropertyToTemplateContentAndModuleBuilder(propertyName);
                moduleBuilder.withVersion(version);
                createModule();
                addTemplateToModule();
            });
        });

        When("^I( try to)? create this module$", (String tryTo) -> createModule(tryTo));

        When("^I try to create this module more than once at the same time$", () -> {
            concurrentCreations = new ArrayList<>();
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> moduleClient.createModule(moduleBuilder.build(), "should-not-fail")));
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> moduleClient.createModule(moduleBuilder.build(), "should-fail")));
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> moduleClient.createModule(moduleBuilder.build(), "should-fail")));
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> moduleClient.createModule(moduleBuilder.build(), "should-fail")));
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> moduleClient.createModule(moduleBuilder.build(), "should-fail")));
        });

        Then("^only one module creation is successful$", () -> {
            long nbFail = concurrentCreations.stream()
                    .map(CompletableFuture::join) // join() récupère le résultat une fois que la requête a abouti
                    .map(ResponseEntity::getStatusCode)
                    .filter(HttpStatus::isError)
                    .count();
            assertThat(nbFail).isGreaterThan(0);
        });

        Then("^the module is actually created$", () -> {
            moduleClient.getModule(moduleBuilder.build());
            assertOK();
        });

        Then("^the module is successfully created$", () -> {
            assertCreated();
            ModuleIO expectedModule = moduleBuilder.build();
            ModuleIO actualModule = testContext.getResponseBody();
            assertEquals(expectedModule, actualModule);
        });

        Then("^the module creation is rejected with a conflict error$", this::assertConflict);

        Then("^the module creation is rejected with a not found error$", this::assertNotFound);

        Then("^the module creation is rejected with a bad request error$", this::assertBadRequest);
    }

    private void addTemplatePropertiesToModuleBuilder(TemplateBuilder templateBuilder) {
        extractPropertyToModuleBuilder(templateBuilder.getFilename());
        extractPropertyToModuleBuilder(templateBuilder.getLocation());
        extractPropertyToModuleBuilder(templateBuilder.getContent());
    }

    private void extractPropertyToModuleBuilder(String input) {
        PropertyBuilder.extractProperties(input).forEach(propertyName -> {
            propertyBuilder.reset().withName(propertyName);
            moduleBuilder.addPropertyBuilder(propertyBuilder);
        });
    }

    private void addPropertyToTemplateContentAndModuleBuilder(String name) {
        propertyBuilder.reset().withName(name);
        addPropertyToTemplateContentAndModuleBuilder(propertyBuilder);
    }

    private void addPropertyToTemplateContentAndModuleBuilder(PropertyBuilder propertyBuilder) {
        templateBuilder.withContent(propertyBuilder.toString());
        moduleBuilder.addPropertyBuilder(propertyBuilder);
    }

    private void createModule() {
        createModule(null);
        assertCreated();
    }

    private void createModule(String tryTo) {
        userAuthorities.ensureUserAuthIsSet();
        moduleClient.createModule(moduleBuilder.build(), tryTo);
        if (isEmpty(tryTo)) {
            moduleBuilder.incrementVersionId();
            moduleHistory.addModuleBuilder(moduleBuilder);
        }
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
}
