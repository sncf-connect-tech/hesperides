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
package org.hesperides.test.bdd.platforms;

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.platforms.*;
import org.hesperides.core.presentation.io.platforms.properties.GlobalPropertyUsageOutput;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.diff.PropertiesDiffOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

@Component
public class PlatformClient {

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity create(PlatformIO platformInput) {
        return create(platformInput, PlatformIO.class);
    }

    public ResponseEntity create(PlatformIO platformInput, Class responseType) {
        return restTemplate.postForEntity(
                "/applications",
                platformInput,
                responseType);
    }

    public ResponseEntity copy(PlatformIO existingPlatform, PlatformIO newPlatform, boolean withoutInstancesAndProperties, Class responseType) {
        String url = "/applications/{application_name}/platforms?from_application={from_application}&from_platform={from_platform}";
        if (withoutInstancesAndProperties) {
            url += "&copy_instances_and_properties=false";
        }
        return restTemplate.postForEntity(
                url,
                newPlatform,
                responseType,
                existingPlatform.getApplicationName(),
                existingPlatform.getApplicationName(),
                existingPlatform.getPlatformName());
    }

    public ResponseEntity delete(PlatformIO platformInput, Class responseType) {
        return restTemplate.exchange(
                "/applications/{application_name}/platforms/{platform_name}",
                HttpMethod.DELETE,
                null,
                responseType,
                platformInput.getApplicationName(),
                platformInput.getPlatformName());
    }

    public ResponseEntity get(PlatformIO platformInput, Long timestamp, boolean withPasswordFlag, Class responseType) {
        String url = "/applications/{application_name}/platforms/{platform_name}";
        if (timestamp != null) {
            url += "?timestamp=" + timestamp;
        }
        if (withPasswordFlag) {
            url += "?with_password_info=true";
        }
        return restTemplate.getForEntity(
                url,
                responseType,
                platformInput.getApplicationName(),
                platformInput.getPlatformName());
    }

    public ResponseEntity update(PlatformIO platformInput, boolean copyProperties, Class responseType) {
        String url = "/applications/{application_name}/platforms";
        if (copyProperties) {
            url += "?copyPropertiesForUpgradedModules=true";
        }
        return restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(platformInput),
                responseType,
                platformInput.getApplicationName());
    }

    public ResponseEntity<SearchResultOutput[]> searchApplication(String search, Class responseType) {
        return restTemplate.postForEntity("/applications/perform_search?name=" + search, null, responseType);
    }

    public ResponseEntity<SearchResultOutput[]> search(String applicationName, String platformName, Class responseType) {
        String url = "/applications/platforms/perform_search?applicationName=" + applicationName;
        if (StringUtils.isNotBlank(platformName)) {
            url += "&platformName=" + platformName;
        }
        return restTemplate.postForEntity(
                url,
                null,
                responseType);
    }

    public ResponseEntity<InstancesModelOutput> getInstancesModel(PlatformIO platform, String propertiesPath) {
        return restTemplate.getForEntity(
                "/applications/{application_name}/platforms/{platform_name}/properties/instance_model?path={path}",
                InstancesModelOutput.class,
                platform.getApplicationName(),
                platform.getPlatformName(),
                propertiesPath);
    }

    public ResponseEntity<PropertiesIO> saveGlobalProperties(PlatformIO platform, PropertiesIO propertiesInput) {
        return saveProperties(platform, propertiesInput, "#");
    }

    public ResponseEntity<PropertiesIO> saveProperties(PlatformIO platformInput, PropertiesIO propertiesInput, String propertiesPath) {
        return saveProperties(platformInput, propertiesInput, propertiesPath, PropertiesIO.class);
    }

    public ResponseEntity<PropertiesIO> saveProperties(PlatformIO platformInput, PropertiesIO propertiesInput, String propertiesPath, Class responseType) {
        return restTemplate.postForEntity(
                "/applications/{application_name}/platforms/{platform_name}/properties?platform_vid={platform_version_id}&path={properties_path}&comment={comment}",
                propertiesInput,
                responseType,
                platformInput.getApplicationName(),
                platformInput.getPlatformName(),
                platformInput.getVersionId(),
                propertiesPath,
                "this is a comment");
    }

    public ResponseEntity getGlobalPropertiesUsage(PlatformIO platform) {
        return restTemplate.exchange(
                "/applications/{application_name}/platforms/{platform_name}/global_properties_usage",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Set<GlobalPropertyUsageOutput>>>() {
                },
                platform.getApplicationName(),
                platform.getPlatformName());
    }

    public ResponseEntity<PropertiesIO> getProperties(PlatformIO platform, String propertiesPath) {
        return getProperties(platform, propertiesPath, null, PropertiesIO.class);
    }

    public ResponseEntity<PropertiesIO> getProperties(PlatformIO platform, String propertiesPath, Long timestamp, Class responseType) {
        String url = "/applications/{application_name}/platforms/{platform_name}/properties?path={properties_path}";
        if (timestamp != null) {
            url += "&timestamp=" + timestamp;
        }
        return restTemplate.getForEntity(
                url,
                responseType,
                platform.getApplicationName(),
                platform.getPlatformName(),
                propertiesPath);
    }

    public ResponseEntity<PropertiesDiffOutput> getPropertiesDiff(PlatformIO fromPlatform, String fromPropertiesPath, PlatformIO toPlatform, String toPropertiesPath, boolean compareStoredValues, Long timestamp, Class responseType) {
        String url = "/applications/{application_name}/platforms/{platform_name}/properties/diff?path={properties_path}&to_application={to_application}&to_platform={to_platform}&to_path={to_path}&compared_stored_values={compared_stored_values}";
        if (timestamp != null) {
            url += "&timestamp=" + timestamp;
        }
        return restTemplate.getForEntity(
                url,
                responseType,
                fromPlatform.getApplicationName(),
                fromPlatform.getPlatformName(),
                fromPropertiesPath,
                toPlatform.getApplicationName(),
                toPlatform.getPlatformName(),
                toPropertiesPath,
                compareStoredValues);
    }

    public ResponseEntity<ModulePlatformsOutput[]> getPlatformsUsingModule(ModuleIO module) {
        return restTemplate.getForEntity(
                "/applications/using_module/{module_name}/{module_version}/{version_type}",
                ModulePlatformsOutput[].class,
                module.getName(),
                module.getVersion(),
                module.getIsWorkingCopy() ? "workingcopy" : "release");
    }

    public ResponseEntity restore(PlatformIO platformInput, Class responseType) {
        return restTemplate.exchange(
                "/applications/{application_name}/platforms/{platform_name}/restore",
                HttpMethod.POST,
                null,
                responseType,
                platformInput.getApplicationName(),
                platformInput.getPlatformName());
    }

    public ResponseEntity<AllApplicationsDetailOutput> getAllApplications() {
        return restTemplate.getForEntity("/applications/platforms", AllApplicationsDetailOutput.class);
    }
}
