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
package org.hesperides.test.bdd.modules.scenarios.templates;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class UpdateModuleTemplates extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public UpdateModuleTemplates() {

        When("^I( try to)? update this module template$", (String tryTo) -> {
            testContext.setResponseEntity(moduleClient.updateTemplate(templateBuilder.build(), moduleBuilder.build(), getResponseType(tryTo, TemplateIO.class)));
        });

        Then("^the module template is successfully updated$", () -> {
            assertOK();
            String expectedNamespace = moduleBuilder.getNamespace();
            TemplateIO expectedTemplate = templateBuilder.withNamespace(expectedNamespace).withVersionId(2).build();
            TemplateIO actualTemplate = (TemplateIO) testContext.getResponseBody();
            assertEquals(expectedTemplate, actualTemplate);
        });

        Then("^the module template update is rejected with a method not allowed error$", () -> {
            assertMethodNotAllowed();
        });

        Then("^the module template update is rejected with a not found error$", () -> {
            assertNotFound();
        });

        Then("^the module template update is rejected with a conflict error$", () -> {
            assertConflict();
        });
    }
}
