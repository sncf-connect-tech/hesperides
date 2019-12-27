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

import io.cucumber.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class CreateModuleTemplates extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public CreateModuleTemplates() {

        When("^I( try to)? add this template to the module$", (String tryTo) -> {
            templateBuilder.withNamespace(moduleBuilder.buildNamespace());
            templateBuilder.withVersionId(0);
            moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build(), tryTo);
            moduleBuilder.addTemplateBuilder(templateBuilder);
        });

        Then("^the template is successfully added to the module$", () -> {
            assertCreated();
            TemplateIO expectedTemplate = moduleBuilder.getLastTemplateBuilder().build();
            TemplateIO actualTemplate = testContext.getResponseBody();
            assertEquals(expectedTemplate, actualTemplate);
        });

        Then("^the module template creation is rejected with a method not allowed error$", this::assertMethodNotAllowed);

        Then("^the module template creation is rejected with a bad request error$", this::assertBadRequest);

        Then("^the module template creation is rejected with a not found error$", this::assertNotFound);

        Then("^the module template creation is rejected with a conflict error$", this::assertConflict);
    }
}
