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
package backup.modules.scenarios.templates;

import org.hesperides.test.bdd.modules.OldModuleClient;
import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class DeleteModuleTemplates extends HesperidesScenario implements En {

    @Autowired
    private OldModuleClient moduleClient;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private OldModuleBuilder moduleBuilder;

    public DeleteModuleTemplates() {

        When("^I( try to)? delete this module template( using an url-encoded template name)?$", (String tryTo, String urlEncodeTemplateName) -> {
            testContext.setResponseEntity(moduleClient.deleteTemplate(
                    templateBuilder.build().getName(),
                    moduleBuilder.build(),
                    getResponseType(tryTo, ResponseEntity.class),
                    StringUtils.isNotEmpty(urlEncodeTemplateName)));
        });

        Then("^the module template is successfully deleted$", () -> {
            assertNoContent();
            testContext.setResponseEntity(moduleClient.getTemplate(templateBuilder.build().getName(), moduleBuilder.build(), String.class));
            assertNotFound();
        });

        Then("^the module template delete is rejected with a method not allowed error$", () -> {
            assertMethodNotAllowed();
        });

        Then("^the module template delete is rejected with a not found error$", () -> {
            assertNotFound();
        });
    }
}
