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
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;
import static org.junit.Assert.assertEquals;

public class GetModuleTemplates implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;

    private ResponseEntity responseEntity;
    private List<PartialTemplateIO> expectedPartialTemplates = new ArrayList<>();

    private static int nbTemplates = 12;

    public GetModuleTemplates() {

        Given("^multiple templates in this module$", () -> {
            for (int i = 0; i < nbTemplates; i++) {
                templateBuilder.withName("template-" + i + 1);
                moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
                expectedPartialTemplates.add(templateBuilder.buildPartialTemplate(moduleBuilder.getNamespace()));
            }
        });

        Given("^a template in this module$", () -> {
            templateBuilder.withName("a-new-template");
            moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
        });

        Given("^a template that doesn't exist in this module$", () -> {
            templateBuilder.withName("nope");
        });

        When("^I( try to)? get the list of templates of this module$", (final String tryTo) -> {
            responseEntity = moduleClient.getTemplates(moduleBuilder.build(), getResponseType(tryTo, PartialTemplateIO[].class));
        });

        When("^I( try to)? get this template in this module$", (final String tryTo) -> {
            responseEntity = moduleClient.getTemplate(templateBuilder.build().getName(), moduleBuilder.build(), getResponseType(tryTo, TemplateIO.class));
        });

        Then("^a list of all the templates of the module is returned$", () -> {
            assertOK(responseEntity);
            List<PartialTemplateIO> actualPartialTemplates = Arrays.asList((PartialTemplateIO[]) responseEntity.getBody());
            assertEquals(expectedPartialTemplates,
                    actualPartialTemplates.stream()
                            .filter(t -> !TemplateBuilder.DEFAULT_NAME.equals(t.getName()))
                            .collect(Collectors.toList()));
        });

        Then("^the module template is successfully returned$", () -> {
            assertOK(responseEntity);
            TemplateIO expectedTemplate = templateBuilder.withNamespace(moduleBuilder.getNamespace()).withVersionId(1).build();
            TemplateIO actualTemplate = (TemplateIO) responseEntity.getBody();
            assertEquals(expectedTemplate, actualTemplate);
        });

        Then("^the module template is not found$", () -> {
            assertNotFound(responseEntity);
        });

        Then("^the module templates is empty$", () -> {
            assertOK(responseEntity);
            assertEquals(0, ((PartialTemplateIO[]) responseEntity.getBody()).length);
        });

        Then("^the template module is not found$", () -> {
            assertNotFound(responseEntity);
        });
    }
}
