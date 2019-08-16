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
package org.hesperides.test.bdd.templatecontainers.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.hesperides.test.bdd.modules.OldModuleClient;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateTemplates extends HesperidesScenario implements En {

    @Autowired
    private OldModuleClient moduleClient;
    @Autowired
    private OldModuleBuilder moduleBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private PropertyBuilder propertyBuilder;

    public CreateTemplates() {

        Given("^a template to create(?: with name \"([^\"]*)\")?(?: with filename \"([^\"]*)\")?(?: with location \"([^\"]*)\")?$", (
                String name, String filename, String location) -> {

            templateBuilder.reset();

            if (StringUtils.isNotEmpty(name)) {
                templateBuilder.withName(name);
            }
            if (StringUtils.isNotEmpty(filename)) {
                templateBuilder.withFilename(filename);
                addPropertiesToModel(filename);
            }
            if (StringUtils.isNotEmpty(location)) {
                templateBuilder.withLocation(location);
                addPropertiesToModel(location);
            }
        });

        Given("^an existing template$", () -> {
            testContext.setResponseEntity(
                    moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build(), TemplateIO.class)
            );
            assertCreated();
        });

        Given("^a template to create with the same name as the existing one$", () -> {
        });

        Given("^a template to create without a name$", () -> {
            templateBuilder.reset().withName("");
        });

        Given("^a template to create without a filename$", () -> {
            templateBuilder.reset().withName("new-template").withFilename("");
        });

        Given("^a template to create without a location", () -> {
            templateBuilder.reset().withName("new-template").withLocation("");
        });
        Given("^this template content", (String templateContent) -> {
            templateBuilder.setContent(templateContent);
        });
    }

    private void addPropertiesToModel(String input) {
        propertyBuilder.extractProperties(input).forEach(property -> {
            propertyBuilder.reset().withName(property);
//            modelBuilder.withProperty(propertyBuilder.build());
        });
    }
}
