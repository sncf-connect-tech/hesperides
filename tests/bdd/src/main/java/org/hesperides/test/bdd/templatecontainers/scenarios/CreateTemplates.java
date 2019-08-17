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
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class CreateTemplates extends HesperidesScenario implements En {

    @Autowired
    private TemplateBuilder templateBuilder;

    public CreateTemplates() {

        //TODO Supprimer to create ?
        Given("^a template to create" +
                "(?: with name \"([^\"]*)\")?" + //TODO named?
                "(?: with filename \"([^\"]*)\")?" +
                "(?: with location \"([^\"]*)\")?$", (
                String name, String filename, String location) -> {

            templateBuilder.reset();

            if (isNotEmpty(name)) {
                templateBuilder.withName(name);
            }
            if (isNotEmpty(filename)) {
                templateBuilder.withFilename(filename);
            }
            if (isNotEmpty(location)) {
                templateBuilder.withLocation(location);
            }
        });

        Given("^a template(?: named \"([^\"]*)\")? with the following content$", (String name, String content) -> {
            if (isNotEmpty(name)) {
                templateBuilder.withName(name);
            }
            templateBuilder.withContent(content);
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
}
