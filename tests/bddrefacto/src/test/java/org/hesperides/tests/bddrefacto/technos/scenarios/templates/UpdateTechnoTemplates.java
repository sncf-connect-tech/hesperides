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
package org.hesperides.tests.bddrefacto.technos.scenarios.templates;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bddrefacto.commons.StepHelper;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoClient;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;
import static org.junit.Assert.assertEquals;

public class UpdateTechnoTemplates implements En {

    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TechnoClient technoClient;

    private ResponseEntity responseEntity;

    public UpdateTechnoTemplates() {

        Given("^a template to update$", () -> {
            templateBuilder.withVersionId(1);
        });

        Given("^a template with an outdated version$", () -> {
            templateBuilder.withVersionId(0);
        });

        When("^I( try to)? update this techno template$", (final String tryTo) -> {
            responseEntity = technoClient.updateTemplate(templateBuilder.build(), technoBuilder.build(), StepHelper.getResponseType(tryTo, TemplateIO.class));
        });

        Then("^the techno template is successfully updated$", () -> {
            assertOK(responseEntity);
            String expectedNamespace = technoBuilder.getNamespace();
            TemplateIO expectedTemplate = templateBuilder.withNamespace(expectedNamespace).withVersionId(2).build();
            TemplateIO actualTemplate = (TemplateIO) responseEntity.getBody();
            assertEquals(expectedTemplate, actualTemplate);
        });

        Then("^the techno template update is rejected with a method not allowed error$", () -> {
            assertMethodNotAllowed(responseEntity);
        });

        Then("^the techno template update is rejected with a not found error$", () -> {
            assertNotFound(responseEntity);
        });

        Then("^the techno template update is rejected with a conflict error$", () -> {
            assertConflict(responseEntity);
        });
    }
}
