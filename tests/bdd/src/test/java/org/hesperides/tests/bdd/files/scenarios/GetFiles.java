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
package org.hesperides.tests.bdd.files.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.files.InstanceFileOutput;
import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.files.FileClient;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.platforms.PlatformBuilder;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.PropertyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class GetFiles extends HesperidesScenario implements En {

    @Autowired
    private FileClient fileClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private PropertyBuilder propertyBuilder;

    private List<InstanceFileOutput> expectedFiles;

    public GetFiles() {

        When("^I( try to)? get the (instance|module)? files$", (String tryTo, String instanceOrModule) -> {
            PlatformIO platform = platformBuilder.buildInput();
            ModuleIO module = moduleBuilder.build();

            Optional<DeployedModuleIO> deployedModule = CollectionUtils.isEmpty(platform.getDeployedModules())
                    ? Optional.empty() : Optional.of(platform.getDeployedModules().get(0));
            String modulePath = deployedModule.map(DeployedModuleIO::getModulePath).orElse("anything");
            boolean simulate = "module".equals(instanceOrModule);
            String instanceName = getInstanceName(deployedModule, simulate);

            testContext.responseEntity = fileClient.getFiles(
                    platform.getApplicationName(),
                    platform.getPlatformName(),
                    modulePath,
                    module.getName(),
                    module.getVersion(),
                    instanceName,
                    module.getIsWorkingCopy(),
                    simulate,
                    HesperidesScenario.getResponseType(tryTo, InstanceFileOutput[].class));

            expectedFiles = new ArrayList<>();
            technoBuilder.getTemplates().forEach(template -> {
                expectedFiles.add(buildInstanceFileOutput(platform, module, modulePath, simulate, instanceName, template, technoBuilder.getNamespace()));
            });
            moduleBuilder.getTemplates().forEach(template -> {
                expectedFiles.add(buildInstanceFileOutput(platform, module, modulePath, simulate, instanceName, template, moduleBuilder.getNamespace()));
            });
        });

        Then("^the files are successfully retrieved$", () -> {
            assertOK();
            List<InstanceFileOutput> actualOutput = Arrays.asList(getBodyAsArray());
            assertEquals(expectedFiles, actualOutput);
        });
    }

    private String getInstanceName(Optional<DeployedModuleIO> deployedModule, boolean simulate) {
        return deployedModule.isPresent() && !CollectionUtils.isEmpty(deployedModule.get().getInstances()) && !simulate
                ? deployedModule.get().getInstances().get(0).getName()
                : "anything";
    }

    private InstanceFileOutput buildInstanceFileOutput(PlatformIO platform, ModuleIO module, String modulePath, boolean simulate, String instanceName, TemplateIO template, String templateNamespace) {
        String location = propertyBuilder.replacePropertiesWithValues(template.getLocation(), platformBuilder);
        String filename = propertyBuilder.replacePropertiesWithValues(template.getFilename(), platformBuilder);
        return new InstanceFileOutput(
                location + "/" + filename,
                "/rest/files"
                        + "/applications/" + platform.getApplicationName()
                        + "/platforms/" + platform.getPlatformName()
                        + "/" + modulePath
                        + "/" + module.getName()
                        + "/" + module.getVersion()
                        + "/instances/" + instanceName
                        + "/" + template.getName()
                        + "?isWorkingCopy=" + module.getIsWorkingCopy()
                        + "&template_namespace=" + templateNamespace
                        + "&simulate=" + simulate,
                new InstanceFileOutput.Rights("rwx", "---", "   ")
        );
    }
}
