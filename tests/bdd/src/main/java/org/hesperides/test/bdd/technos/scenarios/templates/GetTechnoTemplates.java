/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.test.bdd.technos.scenarios.templates;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class GetTechnoTemplates extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private TechnoBuilder technoBuilder;

    public GetTechnoTemplates() {

        Given("^multiple templates in this techno$", () -> IntStream.range(0, 12).forEach(index -> addTemplateToExistingTechno("template-" + index + 1)));

        Given("^a template in this techno$", () -> addTemplateToExistingTechno("a-new-template"));

        Given("^a template that doesn't exist in this techno$", () -> templateBuilder.withName("doesn-t-exist"));

        When("^I( try to)? get the list of templates of this techno$", (String tryTo) -> technoClient.getTemplates(technoBuilder.build(), tryTo));

        When("^I( try to)? get this template in this techno$", (String tryTo) ->
                technoClient.getTemplate(templateBuilder.getName(), technoBuilder.build(), tryTo));

        Then("^a list of all the templates of the techno is returned$", () -> {
            assertOK();
            List<PartialTemplateIO> expectedPartialTemplates = technoBuilder.getTemplateBuilders().stream().map(TemplateBuilder::buildPartialTemplate).collect(Collectors.toList());
            List<PartialTemplateIO> actualPartialTemplates = Arrays.asList(testContext.getResponseBody(PartialTemplateIO[].class));
            assertEquals(expectedPartialTemplates, actualPartialTemplates);
        });

        Then("^the techno template is successfully returned$", () -> {
            assertOK();
            // On récupère le template depuis la techno pour avoir le bon version_id
            TemplateIO expectedTemplate = technoBuilder.getLastTemplateBuilder().build();
            TemplateIO actualTemplate = testContext.getResponseBody(TemplateIO.class);
            assertEquals(expectedTemplate, actualTemplate);
        });

        Then("^the techno template is not found$", this::assertNotFound);

        Then("^the templates techno is not found$", this::assertNotFound);

        Then("^the list of techno templates is empty$", () -> assertEquals(0, testContext.getResponseBodyArrayLength()));
    }

    private void addTemplateToExistingTechno(String templateName) {
        templateBuilder.reset()
                .withNamespace(technoBuilder.buildNamespace())
                .withName(templateName);
        technoClient.addTemplate(templateBuilder.build(), technoBuilder.build());
        assertCreated();
        technoBuilder.addTemplateBuilder(templateBuilder);
    }
}
