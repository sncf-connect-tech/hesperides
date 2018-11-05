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
package org.hesperides.tests.bdd.templatecontainers.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.ModuleClient;
import org.hesperides.tests.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bdd.commons.StepHelper.assertCreated;

public class CreateTemplates implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;

    public CreateTemplates() {

        Given("^an existing template$", () -> {
            ResponseEntity responseEntity = moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build(), TemplateIO.class);
            assertCreated(responseEntity);
        });

        Given("^a template to create( with the same name as the existing one)?$", (String withTheSameName) -> {
            if (StringUtils.isEmpty(withTheSameName)) {
                templateBuilder.withName("new-template");
            }
        });

        Given("^a template to create without a name$", () -> {
            templateBuilder.withName("");
        });

        Given("^a template to create without a filename$", () -> {
            templateBuilder.withName("new-template").withFilename("");
        });

        Given("^a template to create without a location", () -> {
            templateBuilder.withName("new-template").withLocation("");
        });
    }
}
