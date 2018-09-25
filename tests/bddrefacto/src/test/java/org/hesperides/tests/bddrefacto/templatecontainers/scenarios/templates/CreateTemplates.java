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
package org.hesperides.tests.bddrefacto.templatecontainers.scenarios.templates;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateTemplates implements En {

    @Autowired
    private TemplateBuilder templateBuilder;

    public CreateTemplates() {

        Given("^a template to create( with the same name as the existing one)?$", (final String withTheSameName) -> {
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
