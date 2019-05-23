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
package org.hesperides.test.regression.validation;

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.files.InstanceFileOutput;
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.stream.Stream;

@Component
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
    public static final String PLATFORM_KEY_PREFIX = "platform";

    public void validate() {
        testApplications();
    }

    private void testApplications() {
        testEndpointAndGetResult("applications", GET_APPLICATIONS, SearchResultOutput[].class).ifPresent(applications ->
                Stream.of(applications)
                        .map(SearchResultOutput::getName)
                        .distinct()
                        .filter(StringUtils::isNotEmpty)
                        .forEach(this::testApplicationDetail));
    }

    private void testApplicationDetail(String applicationName) {
        testEndpointAndGetResult("application_detail", GET_APPLICATION_DETAIL, ApplicationOutput.class, applicationName).ifPresent(application ->
                application.getPlatforms().forEach(this::testPlatformEndpoints));
    }

    private void testPlatformEndpoints(PlatformIO platform) {
        String applicationName = platform.getApplicationName();
        String platformName = platform.getPlatformName();
        String platformKey = String.format("%s-%s-%s", PLATFORM_KEY_PREFIX, applicationName, platformName);

        if (StringUtils.isNotEmpty(applicationName) && StringUtils.isNotEmpty(platformName)) {
            testEndpoint(platformKey, GET_PLATFORM_DETAIL, PlatformIO.class, applicationName, platformName);
            testEndpoint(platformKey, GET_GLOBAL_PROPERTIES_USAGE, String.class, applicationName, platformName);
            testEndpoint(platformKey, GET_GLOBAL_PROPERTIES, String.class, applicationName, platformName);
            platform.getDeployedModules().forEach(deployedModule -> testPropertiesEndpoints(platformKey, applicationName, platformName, deployedModule));
        }
    }

    private void testPropertiesEndpoints(String platformKey, String applicationName, String platformName, DeployedModuleIO deployedModule) {
        String propertiesPath = deployedModule.getPropertiesPath();
        if (StringUtils.isNotEmpty(propertiesPath)) {
            testEndpoint(platformKey, GET_PROPERTIES, PlatformIO.class, applicationName, platformName, propertiesPath);
            testEndpoint(platformKey, GET_INSTANCE_MODEL, PlatformIO.class, applicationName, platformName, propertiesPath);
        }
        testFilesEndpoint(platformKey, applicationName, platformName, deployedModule);
    }

    private void testFilesEndpoint(String platformKey, String applicationName, String platformName, DeployedModuleIO deployedModule) {
        if (StringUtils.isNotEmpty(deployedModule.getModulePath())) {
            if (CollectionUtils.isEmpty(deployedModule.getInstances())) {
                testFiles(platformKey, applicationName, platformName, deployedModule, DEFAULT_INSTANCE_NAME);
            } else {
                deployedModule.getInstances().forEach(instance ->
                        testFiles(platformKey, applicationName, platformName, deployedModule, instance.getName()));
            }
        }
    }

    private void testFiles(String platformKey, String applicationName, String platformName, DeployedModuleIO deployedModule, String instanceName) {
        testEndpointAndGetResult(platformKey, GET_FILES, InstanceFileOutput[].class,
                applicationName,
                platformName,
                deployedModule.getModulePath(),
                deployedModule.getName(),
                deployedModule.getVersion(),
                instanceName,
                deployedModule.getIsWorkingCopy(),
                DEFAULT_INSTANCE_NAME.equals(instanceName))
                .ifPresent(files -> Stream.of(files).forEach(file ->
                        testFileEndpoint(platformKey, file.getUrl().substring("/rest/".length()))));
    }
}
