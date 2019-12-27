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

import io.cucumber.java8.En;
import org.hesperides.core.presentation.io.files.InstanceFileOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.files.FileBuilder;
import org.hesperides.test.bdd.files.FileClient;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetFiles extends HesperidesScenario implements En {

    @Autowired
    private FileClient fileClient;
    @Autowired
    private FileBuilder fileBuilder;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetFiles() {

        When("^I( try to)? get the (instance|module)? files(?: in the logical group \"([^\"]*)\")?$", (
                String tryTo, String instanceOrModule, String logicalGroup) -> {

            fileBuilder.setSimulate("module".equals(instanceOrModule));
            fileClient.getFiles(
                    platformBuilder.getApplicationName(),
                    platformBuilder.getPlatformName(),
                    fileBuilder.buildModulePath(),
                    moduleBuilder.getName(),
                    moduleBuilder.getVersion(),
                    fileBuilder.buildInstanceName(),
                    moduleBuilder.isWorkingCopy(),
                    fileBuilder.isSimulate(),
                    tryTo);
        });

        Then("^the files are successfully retrieved$", () -> {
            assertOK();
            List<InstanceFileOutput> expectedFiles = fileBuilder.buildInstanceFileOutputs();
            List<InstanceFileOutput> actualFiles = testContext.getResponseBodyAsList();
            assertEquals(expectedFiles, actualFiles);
        });

        Then("^the JSON output does not contain escaped characters$", () -> {
            assertOK();
            String actualOutput = testContext.getResponseBody();
            assertThat(actualOutput, not(containsString("\\u003")));
        });

        Then("^the file location is \"([^\"]*)\"$", (String expectedLocation) -> {
            assertOK();
            List<InstanceFileOutput> actualOutput = testContext.getResponseBodyAsList();
            assertEquals(expectedLocation, actualOutput.get(0).getLocation());
        });

        Then("^their location contains no mustaches$", () -> {
            assertOK();
            List<InstanceFileOutput> files = testContext.getResponseBodyAsList();
            files.forEach(file -> {
                assertThat(file.getLocation(), not(containsString("{{")));
                assertThat(file.getLocation(), not(containsString("}}")));
            });
        });
    }
}
