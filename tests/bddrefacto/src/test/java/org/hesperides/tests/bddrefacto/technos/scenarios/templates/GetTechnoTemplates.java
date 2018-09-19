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
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bddrefacto.commons.StepHelper;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoClient;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class GetTechnoTemplates implements En {

    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TechnoClient technoClient;

    private ResponseEntity responseEntity;

    public GetTechnoTemplates() {

        Given("^multiple templates in this techno$", () -> {
            for (int i = 0; i < 12; i++) {
                templateBuilder.withName("template-" + i + 1);
                technoClient.addTemplate(templateBuilder.build(), technoBuilder.build(), TemplateIO.class);
            }
        });

        Given("^a template in this techno$", () -> {
            templateBuilder.withName("a-new-template");
            technoClient.addTemplate(templateBuilder.build(), technoBuilder.build(), TemplateIO.class);
        });

        Given("^a template that doesn't exist in this techno$", () -> {
            templateBuilder.withName("nope");
        });

        When("^I( try to)? get the list of templates of this techno$", (final String tryTo) -> {
            responseEntity = technoClient.getTemplates(technoBuilder.build(), StepHelper.getResponseType(tryTo, PartialTemplateIO[].class));
        });

        When("^I( try to)? get this template in this techno$", (final String tryTo) -> {
            responseEntity = technoClient.getTemplate(templateBuilder.build().getName(), technoBuilder.build(), StepHelper.getResponseType(tryTo, TemplateIO.class));
        });

        Then("^a list of all the templates of the techno is returned$", () -> {
            Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            PartialTemplateIO[] templates = (PartialTemplateIO[]) responseEntity.getBody();
        });

        Then("^the techno template is successfully returned$", () -> {
            Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        });

        Then("^the techno template is not found$", () -> {
            Assert.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        });

        Then("^the templates techno is not found$", () -> {
            Assert.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        });

        Then("^the template techno is not found$", () -> {
            Assert.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        });
    }
}
