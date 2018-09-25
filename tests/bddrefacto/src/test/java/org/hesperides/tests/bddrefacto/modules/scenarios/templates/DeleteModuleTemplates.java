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
import org.hesperides.tests.bddrefacto.commons.StepHelper;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;

public class DeleteModuleTemplates implements En {

    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleClient moduleClient;

    private ResponseEntity responseEntity;

    public DeleteModuleTemplates() {

        When("^I( try to)? delete this module template$", (final String tryTo) -> {
            responseEntity = moduleClient.deleteTemplate(templateBuilder.build().getName(), moduleBuilder.build(), StepHelper.getResponseType(tryTo, ResponseEntity.class));
        });

        Then("^the module template is successfully deleted$", () -> {
            assertNoContent(responseEntity);
            moduleClient.getTemplate(templateBuilder.build().getName(), moduleBuilder.build(), String.class);
        });

        Then("^the module template delete is rejected with a method not allowed error$", () -> {
            assertMethodNotAllowed(responseEntity);
        });

        Then("^the module template delete is rejected with a not found error$", () -> {
            assertNotFound(responseEntity);
        });
    }
}
