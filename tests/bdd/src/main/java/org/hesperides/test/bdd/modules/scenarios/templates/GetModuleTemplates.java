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
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class GetModuleTemplates extends HesperidesScenario implements En {

    private static int nbTemplates = 12;
    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    private List<TemplateBuilder> expectedTemplates = new ArrayList<>();

    public GetModuleTemplates() {

        Given("^multiple templates in this module$", () -> {
            for (int i = 0; i < nbTemplates; i++) {
                TemplateBuilder newTemplateBuilder = new TemplateBuilder().withName("template-" + i + 1);
                moduleClient.addTemplate(newTemplateBuilder.build(), moduleBuilder.build());
                expectedTemplates.add(newTemplateBuilder);
            }
        });

        Given("^a template that doesn't exist in this module$", () -> {
            templateBuilder.withName("nope");
        });

        Given("^a template with \"(.)\" within the title$", (String specialCaracter) -> {
            if (StringUtils.isNotEmpty(specialCaracter)) {
                templateBuilder.withName("conf" + specialCaracter + "domains.json");
            }
        });

        When("^I( try to)? get the list of templates of this module$", (String tryTo) -> {
            testContext.setResponseEntity(moduleClient.getTemplates(moduleBuilder.build(), getResponseType(tryTo, PartialTemplateIO[].class)));
        });

        When("^I( try to)? get this template in this module( using an url-encoded template name)?$", (String tryTo, String urlEncodedtemplateName) -> {
            testContext.setResponseEntity(moduleClient.getTemplate(templateBuilder.build().getName(), moduleBuilder.build(), getResponseType(tryTo, TemplateIO.class), StringUtils.isNotEmpty(urlEncodedtemplateName)));
        });

        Then("^a list of all the templates of the module is returned$", () -> {
            assertOK();
            List<PartialTemplateIO> expectedPartialTemplates = expectedTemplates.stream().map(templateBuilder -> templateBuilder.buildPartialTemplate()).collect(Collectors.toList());
            List<PartialTemplateIO> actualPartialTemplates = Arrays.asList(testContext.getResponseBody(PartialTemplateIO[].class));
            assertEquals(expectedPartialTemplates,
                    actualPartialTemplates.stream()
                            .filter(template -> !TemplateBuilder.DEFAULT_NAME.equals(template.getName()))
                            .collect(Collectors.toList()));
        });

        Then("^the module template is successfully returned$", () -> {
            assertOK();
            TemplateIO expectedTemplate = templateBuilder.withNamespace(moduleBuilder.getNamespace()).withVersionId(1).build();
            TemplateIO actualTemplate = testContext.getResponseBody(TemplateIO.class);
            assertEquals(expectedTemplate, actualTemplate);
        });
    }
}
