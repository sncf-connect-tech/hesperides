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
package org.hesperides.tests.bdd.technos.scenarios.templates;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.technos.TechnoClient;
import org.hesperides.tests.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bdd.commons.HesperidesScenario.*;
import static org.junit.Assert.assertEquals;

public class CreateTechnoTemplates extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private TechnoBuilder technoBuilder;

    public CreateTechnoTemplates() {

        When("^I( try to)? add this template to the techno$", (String tryTo) -> {
            testContext.responseEntity = technoClient.addTemplate(templateBuilder.build(), technoBuilder.build(), getResponseType(tryTo, TemplateIO.class));
        });

        Then("^the template is successfully added to the techno$", () -> {
            assertCreated();
            TemplateIO expectedTemplate = templateBuilder.withNamespace(technoBuilder.getNamespace()).withVersionId(1).build();
            TemplateIO actualTemplate = (TemplateIO) testContext.responseEntity.getBody();
            assertEquals(expectedTemplate, actualTemplate);
        });

        Then("^the techno template creation is rejected with a method not allowed error$", () -> {
            assertMethodNotAllowed();
        });

        Then("^the techno template creation is rejected with a bad request error$", () -> {
            assertBadRequest();
        });
    }
}
