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
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class DeleteTechnoTemplates extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private TechnoBuilder technoBuilder;

    public DeleteTechnoTemplates() {

        When("^I( try to)? delete this techno template$", (String tryTo) -> {
            String templateName = templateBuilder.getName();
            technoClient.deleteTemplate(templateName, technoBuilder.build(), getResponseType(tryTo, ResponseEntity.class));
            technoBuilder.removeTemplateBuilderInstance(templateName);
        });

        Then("^the techno template is successfully deleted$", () -> {
            assertOK();
            technoClient.getTemplate(templateBuilder.build().getName(), technoBuilder.build(), String.class);
            assertNotFound();
        });

        Then("^the techno template delete is rejected with a method not allowed error$", this::assertMethodNotAllowed);

        Then("^the techno template delete is rejected with a not found error$", this::assertNotFound);
    }
}
