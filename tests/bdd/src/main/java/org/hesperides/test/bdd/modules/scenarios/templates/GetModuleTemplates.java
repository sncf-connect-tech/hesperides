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
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;

public class GetModuleTemplates extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetModuleTemplates() {

        Given("^multiple templates in this module$", () -> IntStream.range(0, 12).forEach(index -> addTemplateToExistingModule("template-" + index + 1)));

        Given("^a template in this module$", () -> addTemplateToExistingModule("a-new-template"));

        Given("^a template that doesn't exist in this module$", () -> templateBuilder.withName("doesn-t-exist"));

        When("^I( try to)? get the list of templates of this module$", (String tryTo) -> moduleClient.getTemplates(moduleBuilder.build(), tryTo));

        When("^I( try to)? get this template in this module( using an url-encoded template name)?$", (
                String tryTo, String urlEncodeTemplateName) ->
                moduleClient.getTemplate(templateBuilder.getName(), moduleBuilder.build(), tryTo, isNotEmpty(urlEncodeTemplateName)));

        Then("^a list of all the templates of the module is returned$", () -> {
            assertOK();
            List<PartialTemplateIO> expectedPartialTemplates = moduleBuilder.getTemplateBuilders().stream()
                    .map(TemplateBuilder::buildPartialTemplate)
                    .collect(Collectors.toList());
            List<PartialTemplateIO> actualPartialTemplates = testContext.getResponseBodyAsList();
            assertEquals(expectedPartialTemplates, actualPartialTemplates);
        });

        Then("^the module template is successfully returned$", () -> {
            assertOK();
            // On récupère le template depuis la module pour avoir le bon version_id
            TemplateIO expectedTemplate = moduleBuilder.getLastTemplateBuilder().build();
            TemplateIO actualTemplate = testContext.getResponseBody();
            assertEquals(expectedTemplate, actualTemplate);
        });

        Then("^the module template is not found$", this::assertNotFound);

        Then("^the templates module is not found$", this::assertNotFound);

        Then("^the list of module templates is empty$", () -> assertEquals(0, testContext.getResponseBodyArrayLength()));
    }

    private void addTemplateToExistingModule(String templateName) {
        templateBuilder.reset()
                .withNamespace(moduleBuilder.buildNamespace())
                .withName(templateName);
        moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
        assertCreated();
        moduleBuilder.addTemplateBuilder(templateBuilder);
    }
}
