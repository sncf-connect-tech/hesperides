package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.technos.TechnoHistory;
import org.hesperides.test.bdd.templatecontainers.VersionTypes;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
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

    public CreateTechnos() {

        Given("^an existing( released)? techno(?: with (?:this|a) template)?( with properties)?( (?:and|with) global properties)?$", (
                String released, String withProperties, String withGlobalProperties) -> {

            if (isNotEmpty(withProperties)) {
                addPropertyToBuilders("techno-foo");
                addPropertyToBuilders("techno-bar");
            }
            if (isNotEmpty(withGlobalProperties)) {
                addPropertyToBuilders("global-techno-foo");
                addPropertyToBuilders("global-techno-bar");
            }

            createTechno();

            if (isNotEmpty(released)) {
                releaseTechno();
            }
        });

        Given("^a techno with (\\d+) versions$", (Integer nbVersions) -> {
            technoBuilder.withName("new-techno");
            IntStream.range(0, nbVersions).forEach(index -> {
                technoBuilder.withVersion("1." + index);
                createTechno();
            });
        });

        Given("^a list of ?(\\d+)? technos( with different names)?(?: with the same name)?$", (String modulesCount, String withDifferentNames) -> {
            int technosToCreateCount = isEmpty(modulesCount) ? 12 : Integer.parseInt(modulesCount);
            IntStream.range(0, technosToCreateCount).forEach(index -> {
                if (isNotEmpty(withDifferentNames)) {
                    technoBuilder.withName("a-techno-" + index);
                } else {
                    technoBuilder.withVersion("0." + (index + 1));
                }
                createTechno();
            });
        });

        Given("^a techno to create(?: with the same name and version)?( but different letter case)?$", (String withDifferentLetterCase) -> {
            if (isNotEmpty(withDifferentLetterCase)) {
                technoBuilder.withName(technoBuilder.getName().toUpperCase());
            }
        });

        When("^I( try to)? create this techno$", (String tryTo) -> {
            testContext.setResponseEntity(technoClient.create(templateBuilder.build(), technoBuilder.build(), getResponseType(tryTo, TemplateIO.class)));
        });

        Then("^the techno is successfully created$", () -> {
            assertCreated();
            TemplateIO expectedTemplate = templateBuilder.build();
            TemplateIO actualTemplate = testContext.getResponseBody(TemplateIO.class);
            assertEquals(expectedTemplate, actualTemplate);
        });

        Then("^the techno creation is rejected with a conflict error$", this::assertConflict);
    }

    private void addPropertyToBuilders(String name) {
        propertyBuilder.reset().withName(name);
        templateBuilder.withContent(propertyBuilder.toString());
        technoBuilder.withPropertyBuilder(propertyBuilder);
    }

    private void createTechno() {
        templateBuilder.withNamespace(technoBuilder.buildNamespace());
        technoClient.create(templateBuilder.buildAndIncrementVersionId(), technoBuilder.build());
        assertOK();
        technoBuilder.withTemplateBuilder(templateBuilder);
        technoHistory.addTechnoBuilder(technoBuilder);
    }

    private void releaseTechno() {
        technoClient.release(technoBuilder.build());
        technoBuilder.withIsWorkingCopy(false);
        assertCreated();
    }
}
