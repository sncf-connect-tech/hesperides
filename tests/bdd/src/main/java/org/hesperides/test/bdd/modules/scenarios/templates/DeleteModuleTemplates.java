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
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class DeleteModuleTemplates extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public DeleteModuleTemplates() {

        When("^I( try to)? delete this module template( using an url-encoded template name)?$", (
                String tryTo, String urlEncodeTemplateName) -> {

            String templateName = templateBuilder.getName();
            moduleClient.deleteTemplate(templateName, moduleBuilder.build(), tryTo, isNotEmpty(urlEncodeTemplateName));

            if (isEmpty(tryTo)) {
                moduleBuilder.removeTemplateBuilder(templateName);
            }
        });

        Then("^the module template is successfully deleted$", () -> {
            assertNoContent();
            moduleClient.getTemplate(templateBuilder.build().getName(), moduleBuilder.build(), "should-fail");
            assertNotFound();
        });

        Then("^the module template delete is rejected with a method not allowed error$", this::assertMethodNotAllowed);

        Then("^the module template delete is rejected with a not found error$", this::assertNotFound);
    }
}
