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
package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.hesperides.tests.bdd.templatecontainers.TemplateAssertions;
import org.hesperides.tests.bdd.templatecontainers.TemplateSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class UpdateATemplate extends CucumberSpringBean implements En {

    @Autowired
    private TechnoContext technoContext;

    private ResponseEntity response;

    public UpdateATemplate() {

        When("^updating the template in this techno", () -> {
            TemplateIO templateInput = TemplateSamples.getTemplateInputWithVersionId(1);
            response = technoContext.updateTemplate(templateInput);
        });

        Then("^the template in this techno is updated", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            TemplateIO templateOutput = (TemplateIO) response.getBody();
            TemplateAssertions.assertTemplateAgainstDefaultValues(templateOutput, technoContext.getNamespace(), 2L);
        });
    }

    // TODO Tester la tentative de modification d'un template qui n'existe pas => 404
}
