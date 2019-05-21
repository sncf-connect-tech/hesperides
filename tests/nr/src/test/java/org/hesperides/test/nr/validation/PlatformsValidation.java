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
package org.hesperides.test.nr.validation;

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.files.InstanceFileOutput;
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.springframework.util.CollectionUtils;

import java.util.stream.Stream;

public class PlatformsValidation extends AbstractValidation {

    private static final String GET_APPLICATIONS = "applications";
    private static final String GET_APPLICATION_DETAIL = "applications/{application}";
    private static final String GET_PLATFORM_DETAIL = "applications/{application}/platforms/{platform}";
    private static final String GET_GLOBAL_PROPERTIES_USAGE = "applications/{application}/platforms/{platform}/global_properties_usage";
    private static final String GET_GLOBAL_PROPERTIES = "applications/{application}/platforms/{platform}/properties?path=#";
    private static final String GET_PROPERTIES = "applications/{application}/platforms/{platform}/properties?path={properties_path}";
    private static final String GET_INSTANCE_MODEL = "applications/{application}/platforms/{platform}/properties/instance_model?path={properties_path}";

    private static final String GET_FILES =
            "files/applications/{application_name}" +
                    "/platforms/{platform_name}" +
                    "/{module_path}/{module_name}/{module_version}" +
                    "/instances/{instance_name}" +
                    "?isWorkingCopy={is_working_copy}" +
                    "&simulate={simulate}";

    private static final String DEFAULT_INSTANCE_NAME = "default";

    public void validate() {

        testAndGetResult(GET_APPLICATIONS, SearchResultOutput[].class).ifPresent(applications -> {
            Stream.of(applications).map(SearchResultOutput::getName).forEach(applicationName -> {
                testAndGetResult(GET_APPLICATION_DETAIL, ApplicationOutput.class, applicationName).ifPresent(application -> {

                    application.getPlatforms().forEach(platform -> {
                        String platformName = platform.getPlatformName();
                        test(GET_PLATFORM_DETAIL, PlatformIO.class, applicationName, platformName);
                        test(GET_GLOBAL_PROPERTIES_USAGE, String.class, applicationName, platformName);
                        test(GET_GLOBAL_PROPERTIES, String.class, applicationName, platformName);

                        platform.getDeployedModules().forEach(deployedModule -> {
                            String propertiesPath = deployedModule.getPropertiesPath();
                            test(GET_PROPERTIES, PlatformIO.class, applicationName, platformName, propertiesPath);
                            test(GET_INSTANCE_MODEL, PlatformIO.class, applicationName, platformName, propertiesPath);

                            if (StringUtils.isNotEmpty(deployedModule.getModulePath())) {
                                if (CollectionUtils.isEmpty(deployedModule.getInstances())) {
                                    testFiles(platform, deployedModule, DEFAULT_INSTANCE_NAME);
                                } else {
                                    deployedModule.getInstances().forEach(instance ->
                                            testFiles(platform, deployedModule, instance.getName()));
                                }
                            }
                        });
                    });
                });
            });
        });
    }

    private void testFiles(PlatformIO platform, DeployedModuleIO deployedModule, String instanceName) {
        testAndGetResult(GET_FILES, InstanceFileOutput[].class,
                platform.getApplicationName(),
                platform.getPlatformName(),
                deployedModule.getModulePath(),
                deployedModule.getName(),
                deployedModule.getVersion(),
                instanceName,
                deployedModule.getIsWorkingCopy(),
                DEFAULT_INSTANCE_NAME.equals(instanceName))
                .ifPresent(files ->
                        Stream.of(files).forEach(file ->
                                test(file.getUrl().substring("/rest/".length()), true, String.class)));
    }
}
