package org.hesperides.test.bdd.technos.scenarios;

import io.cucumber.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.technos.TechnoHistory;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class CreateTechnos extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TechnoHistory technoHistory;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private PropertyBuilder propertyBuilder;
    @Autowired
    private ReleaseTechnos releaseTechnos;

    private List<CompletableFuture<ResponseEntity>> concurrentCreations;

    public CreateTechnos() {

        Given("^an existing( released)? techno" +
                "(?: named \"([^\"]*)\")?" +
                "(?: with (?:this|a) template)?" +
                "( with properties)?" +
                "( (?:and|with) global properties)?" +
                "( (?:and|with) iterable properties)?" +
                "( (?:and|with) nested iterable properties)?$", (
                String released,
                String name,
                String withProperties,
                String withGlobalProperties,
                String withIterableProperties,
                String withNestedIterableProperties) -> {

            technoBuilder.reset();

            if (isNotEmpty(name)) {
                technoBuilder.withName(name);
            }

            if (isNotEmpty(withProperties)) {
                addPropertyToBuilders("techno-foo");
                addPropertyToBuilders("techno-bar");
            }
            if (isNotEmpty(withGlobalProperties)) {
                addPropertyToBuilders("global-techno-foo");
                addPropertyToBuilders("global-techno-bar");
            }
            if (isNotEmpty(withIterableProperties)) {
                propertyBuilder.reset()
                        .withName("techno-foo")
                        .withProperty(new PropertyBuilder()
                                .withName("techno-bar"));
                addPropertyToBuilders(propertyBuilder);
            }
            if (isNotEmpty(withNestedIterableProperties)) {
                propertyBuilder.reset()
                        .withName("techno-foo")
                        .withProperty(new PropertyBuilder()
                                .withName("techno-bar")
                                .withProperty(new PropertyBuilder()
                                        .withName("techno-foobar")));
                addPropertyToBuilders(propertyBuilder);
            }

            createTechno();

            if (isNotEmpty(released)) {
                releaseTechnos.release(null);
                assertCreated();
            }
        });

        Given("^a techno with (\\d+) versions$", (Integer nbVersions) -> {
            technoBuilder.withName("new-techno");
            IntStream.range(0, nbVersions).forEach(index -> {
                technoBuilder.withVersion("1." + index);
                createTechno();
            });
        });

        Given("^a list of ?(\\d+)? technos( with different names)?(?: with the same name)?$", (Integer technosCount, String withDifferentNames) -> {
            int technosToCreateCount = technosCount == null ? 12 : technosCount;
            IntStream.range(0, technosToCreateCount).forEach(index -> {
                if (isNotEmpty(withDifferentNames)) {
                    technoBuilder.withName("a-techno-" + index);
                } else {
                    technoBuilder.withVersion("0." + (index + 1));
                }
                createTechno();
            });
        });

        Given("^an existing techno with properties with the same name and comment, but different default values, in two templates$", () -> {
            createTechno();

            templateBuilder.reset().withName("template-a").withNamespace(technoBuilder.buildNamespace());
            propertyBuilder.reset().withName("foo").withComment("comment").withDefaultValue("12");
            addPropertyToBuilders(propertyBuilder);
            technoClient.addTemplate(templateBuilder.build(), technoBuilder.build());
            assertCreated();
            technoBuilder.addTemplateBuilder(templateBuilder);

            templateBuilder.reset().withName("template-b").withNamespace(technoBuilder.buildNamespace());
            propertyBuilder.reset().withName("foo").withComment("comment").withDefaultValue("42");
            addPropertyToBuilders(propertyBuilder);
            technoClient.addTemplate(templateBuilder.build(), technoBuilder.build());
            assertCreated();
            technoBuilder.addTemplateBuilder(templateBuilder);
        });

        Given("^an existing techno with properties with the same name but different comments in two templates$", () -> {
            createTechno();

            templateBuilder.reset().withName("template-a").withNamespace(technoBuilder.buildNamespace());
            propertyBuilder.reset().withName("foo").withComment("comment-a");
            addPropertyToBuilders(propertyBuilder);
            technoClient.addTemplate(templateBuilder.build(), technoBuilder.build());
            assertCreated();
            technoBuilder.addTemplateBuilder(templateBuilder);

            templateBuilder.reset().withName("template-b").withNamespace(technoBuilder.buildNamespace());
            propertyBuilder.reset().withName("foo").withComment("comment-b");
            addPropertyToBuilders(propertyBuilder);
            technoClient.addTemplate(templateBuilder.build(), technoBuilder.build());
            assertCreated();
            technoBuilder.addTemplateBuilder(templateBuilder);
        });

        Given("^the techno template properties are modified$", () -> {
            addPropertyToBuilders("patate");
            technoClient.updateTemplate(templateBuilder.build(), technoBuilder.build());
            assertOK();
            technoBuilder.updateTemplateBuilder(templateBuilder);
        });

        Given("^a techno to create(?: with the same name and version)?( but different letter case)?$", (String withDifferentLetterCase) -> {
            if (isNotEmpty(withDifferentLetterCase)) {
                technoBuilder.withName(technoBuilder.getName().toUpperCase());
            }
        });

        When("^I( try to)? create this techno$", (String tryTo) -> createTechno(tryTo));

        When("^I try to create this techno more than once at the same time$", () -> {
            concurrentCreations = new ArrayList<>();
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> technoClient.createTechno(templateBuilder.build(), technoBuilder.build(), "should-not-fail")));
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> technoClient.createTechno(templateBuilder.build(), technoBuilder.build(), "should-fail")));
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> technoClient.createTechno(templateBuilder.build(), technoBuilder.build(), "should-fail")));
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> technoClient.createTechno(templateBuilder.build(), technoBuilder.build(), "should-fail")));
            concurrentCreations.add(CompletableFuture.supplyAsync(() -> technoClient.createTechno(templateBuilder.build(), technoBuilder.build(), "should-fail")));
        });

        Then("^only one techno creation is successful$", () -> {
            long nbFail = concurrentCreations.stream()
                    .map(CompletableFuture::join)
                    .map(ResponseEntity::getStatusCode)
                    .filter(HttpStatus::isError)
                    .count();
            assertThat(nbFail).isGreaterThan(0);
        });

        Then("^the techno is actually created$", () -> {
            technoClient.getTechno(technoBuilder.build());
            assertOK();
        });

        Then("^the techno is successfully created$", () -> {
            assertCreated();
            TemplateIO expectedTemplate = technoBuilder.getLastTemplateBuilder().build();
            TemplateIO actualTemplate = testContext.getResponseBody();
            assertEquals(expectedTemplate, actualTemplate);
        });

        Then("^the techno creation is rejected with a conflict error$", this::assertConflict);
    }

    private void addPropertyToBuilders(String name) {
        propertyBuilder.reset().withName(name);
        addPropertyToBuilders(propertyBuilder);
    }

    private void addPropertyToBuilders(PropertyBuilder propertyBuilder) {
        templateBuilder.withContent(propertyBuilder.toString());
        technoBuilder.addPropertyBuilder(propertyBuilder);
    }

    private void createTechno() {
        createTechno(null);
        assertCreated();
    }

    private void createTechno(String tryTo) {
        templateBuilder
                .withVersionId(0)
                .withNamespace(technoBuilder.buildNamespace());
        technoClient.createTechno(templateBuilder.build(), technoBuilder.build(), tryTo);
        if (isEmpty(tryTo)) {
            technoBuilder.addTemplateBuilder(templateBuilder);
            technoHistory.addTechnoBuilder(technoBuilder);
        }
    }
}
