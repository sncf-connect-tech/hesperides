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
package org.hesperides.test.bdd.files.scenarios;

import cucumber.api.java8.En;
import org.assertj.core.api.Assertions;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.files.FileBuilder;
import org.hesperides.test.bdd.files.FileClient;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.Assert.assertEquals;

public class GetFile extends HesperidesScenario implements En {

    @Autowired
    private FileClient fileClient;
    @Autowired
    private FileBuilder fileBuilder;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;

    public GetFile() {

        When("^I( try to)? get the (instance|module)? template file(?: for instance(?: named \"([^\"]*)\")?)?$", (
                String tryTo, String instanceOrModule, String instanceName) -> {

            if (isEmpty(instanceName)) {
                instanceName = fileBuilder.buildInstanceName();
            }

            fileBuilder.setSimulate("module".equals(instanceOrModule));

            fileClient.getFile(
                    platformBuilder.getApplicationName(),
                    platformBuilder.getPlatformName(),
                    fileBuilder.buildModulePath(),
                    moduleBuilder.getName(),
                    moduleBuilder.getVersion(),
                    instanceName,
                    templateBuilder.getName(),
                    moduleBuilder.isWorkingCopy(),
                    moduleBuilder.buildNamespace(),
                    fileBuilder.isSimulate());
        });

        Then("^the file is successfully retrieved and contains$", (String fileContent) -> {
            assertOK();
            String expectedOutput = fileContent.replaceAll("&nbsp;", "");
            String actualOutput = testContext.getResponseBody();
            assertEquals(expectedOutput, defaultString(actualOutput, ""));
        });

        Then("^there are( no)? obfuscated password properties in the(?: initial)? file$", (String no) -> {
            String actualOutput = testContext.getResponseBody();
            if (isEmpty(no)) {
                Assertions.assertThat(actualOutput).contains("********");
            } else {
                Assertions.assertThat(actualOutput).doesNotContain("********");
            }
        });
    }
}
