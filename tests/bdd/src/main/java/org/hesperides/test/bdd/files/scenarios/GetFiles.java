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
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.files.InstanceFileOutput;
import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.files.FileClient;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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

        When("^I( try to)? get the (instance|module)? files(?: in the logical group \"([^\"]*)\")?$", (String tryTo, String instanceOrModule, String logicalGroup) -> {
            PlatformIO platform = platformBuilder.buildInput();
            ModuleIO module = moduleBuilder.build();

            Optional<DeployedModuleIO> deployedModule = getDeployedModule(platform, logicalGroup);
            String modulePath = deployedModule.map(DeployedModuleIO::getModulePath).orElse("anything");
            boolean simulate = "module".equals(instanceOrModule);
            String instanceName = getInstanceName(deployedModule, simulate);

            testContext.setResponseEntity(fileClient.getFiles(
                    platform.getApplicationName(),
                    platform.getPlatformName(),
                    modulePath,
                    module.getName(),
                    module.getVersion(),
                    instanceName,
                    module.getIsWorkingCopy(),
                    simulate,
                    HesperidesScenario.getResponseType(tryTo, InstanceFileOutput[].class)));

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

        Then("^the JSON output does not contain escaped characters$", () -> {
            assertOK();
            String actualOutput = testContext.getResponseBody(String.class);
            assertThat(actualOutput, not(containsString("\\u003")));
        });

        Then("^the file location is \"([^\"]*)\"$", (String expectedLocation) -> {
            assertOK();
            List<InstanceFileOutput> actualOutput = Arrays.asList(getBodyAsArray());
            assertEquals(expectedLocation, actualOutput.get(0).getLocation());
        });

        Then("^their location contains no mustaches$", () -> {
            assertOK();
            List<InstanceFileOutput> files = Arrays.asList(getBodyAsArray());
            files.forEach(file -> {
                assertThat(file.getLocation(), not(containsString("{{")));
                assertThat(file.getLocation(), not(containsString("}}")));
            });
        });
    }

    private Optional<DeployedModuleIO> getDeployedModule(PlatformIO platform, String logicalGroup) {
        return platform.getDeployedModules()
                .stream()
                .filter(deployedModuleIO -> StringUtils.isEmpty(logicalGroup) || deployedModuleIO.getModulePath().equalsIgnoreCase("#" + logicalGroup))
                .findFirst();
    }

    private String getInstanceName(Optional<DeployedModuleIO> deployedModule, boolean simulate) {
        return deployedModule.isPresent() && !CollectionUtils.isEmpty(deployedModule.get().getInstances()) && !simulate
                ? deployedModule.get().getInstances().get(0).getName()
                : "anything";
    }

    private InstanceFileOutput buildInstanceFileOutput(PlatformIO platform, ModuleIO module, String modulePath, boolean simulate, String instanceName, TemplateIO template, String templateNamespace) {
        String location = replacePropertiesWithValues(template.getLocation(), modulePath, instanceName);
        String filename = replacePropertiesWithValues(template.getFilename(), modulePath, instanceName);
        try {
            return new InstanceFileOutput(
                    location + "/" + filename,
                    "/rest/files"
                            + "/applications/" + platform.getApplicationName()
                            + "/platforms/" + platform.getPlatformName()
                            + "/" + URLEncoder.encode(modulePath, "UTF-8")
                            + "/" + module.getName()
                            + "/" + module.getVersion()
                            + "/instances/" + instanceName
                            + "/" + template.getName()
                            + "?isWorkingCopy=" + module.getIsWorkingCopy()
                            + "&template_namespace=" + URLEncoder.encode(templateNamespace, "UTF-8")
                            + "&simulate=" + simulate,
                    new InstanceFileOutput.Rights("rwx", "---", "   ")
            );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cf. org.hesperides.core.application.files.FileUseCases#valorizeWithModuleAndGlobalAndInstanceProperties
     * <p>
     * 1. Une propriété peut être valorisée au niveau de la platforme (propriété globale)
     * et au niveau du module (valorisation classique).
     * <p>
     * 2. La valorisation d'une propriété au niveau du module peut faire référence
     * à une propriété globale ou d'instance.
     * <p>
     * 3. La valorisation d'une propriété d'instance peut faire référence
     * à une propriété globale.
     */
    private String replacePropertiesWithValues(String input, String modulePath, String instanceName) {
        List<ValuedPropertyIO> predefinedProperties = getPredefinedProperties(modulePath, instanceName);

        List<ValuedPropertyIO> moduleAndGlobalProperties = platformBuilder.getModuleAndGlobalProperties();
        input = propertyBuilder.replacePropertiesWithValues(input, predefinedProperties, moduleAndGlobalProperties);
        List<ValuedPropertyIO> globalProperties = platformBuilder.getAllGlobalProperties();
        List<ValuedPropertyIO> globalAndInstanceProperties = new ArrayList<>(globalProperties);
        globalAndInstanceProperties.addAll(platformBuilder.getInstancePropertyValues());
        input = propertyBuilder.replacePropertiesWithValues(input, predefinedProperties, globalAndInstanceProperties);
        return propertyBuilder.replacePropertiesWithValues(input, predefinedProperties, globalProperties);
    }

    private List<ValuedPropertyIO> getPredefinedProperties(String modulePath, String instanceName) {
        PlatformIO platform = platformBuilder.buildInput();
        ModuleIO module = moduleBuilder.build();
        return Arrays.asList(
                new ValuedPropertyIO("hesperides.application.name", platform.getApplicationName()),
                new ValuedPropertyIO("hesperides.application.version", platform.getVersion()),
                new ValuedPropertyIO("hesperides.platform.name", platform.getPlatformName()),
                new ValuedPropertyIO("hesperides.module.name", module.getName()),
                new ValuedPropertyIO("hesperides.module.version", module.getVersion()),
                new ValuedPropertyIO("hesperides.module.path.full", modulePath.replace('#', '/')),
                new ValuedPropertyIO("hesperides.instance.name", instanceName));
    }
}
