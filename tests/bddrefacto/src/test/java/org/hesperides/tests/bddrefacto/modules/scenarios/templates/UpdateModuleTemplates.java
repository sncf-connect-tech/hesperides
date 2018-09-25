/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-modulelogies/hesperides)
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
package org.hesperides.tests.bddrefacto.modules.scenarios.templates;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bddrefacto.commons.StepHelper;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;
import static org.junit.Assert.assertEquals;

public class UpdateModuleTemplates implements En {

    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleClient moduleClient;

    private ResponseEntity responseEntity;

    public UpdateModuleTemplates() {

        When("^I( try to)? update this module template$", (final String tryTo) -> {
            responseEntity = moduleClient.updateTemplate(templateBuilder.build(), moduleBuilder.build(), StepHelper.getResponseType(tryTo, TemplateIO.class));
        });

        Then("^the module template is successfully updated$", () -> {
            assertOK(responseEntity);
            String expectedNamespace = moduleBuilder.getNamespace();
            TemplateIO expectedTemplate = templateBuilder.withNamespace(expectedNamespace).withVersionId(2).build();
            TemplateIO actualTemplate = (TemplateIO) responseEntity.getBody();
            assertEquals(expectedTemplate, actualTemplate);
        });

        Then("^the module template update is rejected with a method not allowed error$", () -> {
            assertMethodNotAllowed(responseEntity);
        });

        Then("^the module template update is rejected with a not found error$", () -> {
            assertNotFound(responseEntity);
        });

        Then("^the module template update is rejected with a conflict error$", () -> {
            assertConflict(responseEntity);
        });
    }
}
