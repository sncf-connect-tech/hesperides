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
import org.apache.commons.lang3.StringUtils;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.files.FileClient;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView.OBFUSCATED_PASSWORD_VALUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetFile extends HesperidesScenario implements En {

    @Autowired
    private FileClient fileClient;
    @Autowired
    private PlatformBuilder oldPlatformBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;

    public GetFile() {

        When("^I( try to)? get the (instance|module)? template file$", (String tryTo, String instanceOrModule) -> {

            //Factoriser avec GetFiles

            Optional<DeployedModuleBuilder> deployedModuleBuilder = CollectionUtils.isEmpty(oldPlatformBuilder.getDeployedModuleBuilders())
                    ? Optional.empty() : Optional.of(oldPlatformBuilder.getDeployedModuleBuilders().get(0));
            String modulePath = deployedModuleBuilder.map(DeployedModuleBuilder::getModulePath).orElse("anything");
            boolean simulate = "module".equals(instanceOrModule);
            String instanceName = getInstanceName(deployedModuleBuilder, simulate);

            fileClient.getFile(
                    oldPlatformBuilder.getApplicationName(),
                    oldPlatformBuilder.getPlatformName(),
                    modulePath,
                    moduleBuilder.getName(),
                    moduleBuilder.getVersion(),
                    instanceName,
                    templateBuilder.getName(),
                    VersionType.toIsWorkingCopy(moduleBuilder.getVersionType()), // Dans ModuleBuilder ?
                    templateBuilder.getNamespace(),
                    simulate);
        });

        Then("^the file is successfully retrieved and contains$", (String fileContent) -> {
            assertOK();
            String expectedOutput = fileContent.replaceAll("&nbsp;", "");
            String actualOutput = testContext.getResponseBody();
            assertEquals(expectedOutput, defaultString(actualOutput, ""));
        });

        Then("^there are( no)? obfuscated password properties in the(?: initial)? file$", (String no) -> {
            String actualOutput = testContext.getResponseBody();
            if (StringUtils.isBlank(no)) {
                assertThat(actualOutput, containsString(OBFUSCATED_PASSWORD_VALUE));
            } else {
                assertThat(actualOutput, not(containsString(OBFUSCATED_PASSWORD_VALUE)));
            }
        });
    }

    private String getInstanceName(Optional<DeployedModuleBuilder> deployedModuleBuilder, boolean simulate) {
        return deployedModuleBuilder.isPresent() && !CollectionUtils.isEmpty(deployedModuleBuilder.get().getInstanceBuilders()) && !simulate
                ? deployedModuleBuilder.get().getInstanceBuilders().get(0).getName()
                : "anything";
    }
}
