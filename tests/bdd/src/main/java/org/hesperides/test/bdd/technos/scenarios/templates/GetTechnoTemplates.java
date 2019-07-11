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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class GetTechnoTemplates extends HesperidesScenario implements En {

    private static int nbTemplates = 12;
    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private TechnoBuilder technoBuilder;
    private List<PartialTemplateIO> expectedPartialTemplates = new ArrayList<>();

    public GetTechnoTemplates() {

        Given("^multiple templates in this techno$", () -> {
            for (int i = 0; i < nbTemplates; i++) {
                templateBuilder.withName("template-" + i + 1);
                technoClient.addTemplate(templateBuilder.build(), technoBuilder.build());
                expectedPartialTemplates.add(templateBuilder.buildPartialTemplate(technoBuilder.getNamespace()));
            }
        });

        Given("^a template in this techno$", () -> {
            templateBuilder.withName("a-new-template");
            technoClient.addTemplate(templateBuilder.build(), technoBuilder.build());
        });

        Given("^a template that doesn't exist in this techno$", () -> {
            templateBuilder.withName("nope");
        });

        When("^I( try to)? get the list of templates of this techno$", (String tryTo) -> {
            testContext.setResponseEntity(technoClient.getTemplates(technoBuilder.build(), getResponseType(tryTo, PartialTemplateIO[].class)));
        });

        When("^I( try to)? get this template in this techno$", (String tryTo) -> {
            testContext.setResponseEntity(technoClient.getTemplate(templateBuilder.build().getName(), technoBuilder.build(), getResponseType(tryTo, TemplateIO.class)));
        });

        Then("^a list of all the templates of the techno is returned$", () -> {
            assertOK();
            List<PartialTemplateIO> actualPartialTemplates = Arrays.asList(testContext.getResponseBody(PartialTemplateIO[].class));
            assertEquals(expectedPartialTemplates,
                    actualPartialTemplates.stream()
                            .filter(t -> !TemplateBuilder.DEFAULT_NAME.equals(t.getName()))
                            .collect(Collectors.toList()));
        });

        Then("^the techno template is successfully returned$", () -> {
            assertOK();
            TemplateIO expectedTemplate = templateBuilder.withNamespace(technoBuilder.getNamespace()).withVersionId(1).build();
            TemplateIO actualTemplate = testContext.getResponseBody(TemplateIO.class);
            assertEquals(expectedTemplate, actualTemplate);
        });

        Then("^the techno template is not found$", () -> {
            assertNotFound();
        });

        Then("^the templates techno is not found$", () -> {
            assertNotFound();
        });

        Then("^the list of techno templates is empty$", () -> {
            assertEquals(0, getBodyAsArray().length);
        });
    }
}
