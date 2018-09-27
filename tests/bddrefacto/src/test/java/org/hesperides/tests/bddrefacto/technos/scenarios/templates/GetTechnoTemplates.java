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
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoClient;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;
import static org.junit.Assert.assertEquals;

public class GetTechnoTemplates implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private TechnoBuilder technoBuilder;

    private ResponseEntity responseEntity;
    private List<PartialTemplateIO> expectedPartialTemplates = new ArrayList<>();

    private static int nbTemplates = 12;

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

        When("^I( try to)? get the list of templates of this techno$", (final String tryTo) -> {
            responseEntity = technoClient.getTemplates(technoBuilder.build(), getResponseType(tryTo, PartialTemplateIO[].class));
        });

        When("^I( try to)? get this template in this techno$", (final String tryTo) -> {
            responseEntity = technoClient.getTemplate(templateBuilder.build().getName(), technoBuilder.build(), getResponseType(tryTo, TemplateIO.class));
        });

        Then("^a list of all the templates of the techno is returned$", () -> {
            assertOK(responseEntity);
            List<PartialTemplateIO> actualPartialTemplates = Arrays.asList((PartialTemplateIO[]) responseEntity.getBody());
            assertEquals(expectedPartialTemplates,
                    actualPartialTemplates.stream()
                            .filter(t -> !TemplateBuilder.DEFAULT_NAME.equals(t.getName()))
                            .collect(Collectors.toList()));
        });

        Then("^the techno template is successfully returned$", () -> {
            assertOK(responseEntity);
            TemplateIO expectedTemplate = templateBuilder.withNamespace(technoBuilder.getNamespace()).withVersionId(1).build();
            TemplateIO actualTemplate = (TemplateIO) responseEntity.getBody();
            assertEquals(expectedTemplate, actualTemplate);
        });

        Then("^the techno template is not found$", () -> {
            assertNotFound(responseEntity);
        });

        Then("^the templates techno is not found$", () -> {
            assertNotFound(responseEntity);
        });

        Then("^the template techno is not found$", () -> {
            assertNotFound(responseEntity);
        });
    }
}
