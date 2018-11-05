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
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.technos.TechnoClient;
import org.hesperides.tests.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bdd.commons.HesperidesScenario.*;

public class DeleteTechnoTemplates extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private TechnoBuilder technoBuilder;

    public DeleteTechnoTemplates() {

        When("^I( try to)? delete this techno template$", (String tryTo) -> {
            responseEntity = technoClient.deleteTemplate(templateBuilder.build().getName(), technoBuilder.build(), getResponseType(tryTo, ResponseEntity.class));
        });

        Then("^the techno template is successfully deleted$", () -> {
            assertOK();
            responseEntity = technoClient.getTemplate(templateBuilder.build().getName(), technoBuilder.build(), String.class);
            assertNotFound();
        });

        Then("^the techno template delete is rejected with a method not allowed error$", () -> {
            assertMethodNotAllowed();
        });

        Then("^the techno template delete is rejected with a not found error$", () -> {
            assertNotFound();
        });
    }
}
