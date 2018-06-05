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
package org.hesperides.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api("/versions")
@RestController
@CrossOrigin
@RequestMapping("/versions")
public class VersionsController extends BaseController {

    @Value("${application.name}")
    private String applicationName;

    @Value("${build.version}")
    private String buildVersion;

    @Value("${build.timestamp}")
    private String buildTimestamp;

    @Value("${app.encoding}")
    private String appEncoding;

    @Value("${app.java.version}")
    private String appJavaVersion;

    @ApiOperation("Get backend and API versions")
    @GetMapping
    public ResponseEntity<Map<String, String>> getVersions() {

        Map<String, String> propertiesMap = new HashMap<>();
        propertiesMap.put("backend_version", buildVersion);
        propertiesMap.put("api_version", buildVersion);
        propertiesMap.put("buildVersion", buildVersion);
        propertiesMap.put("applicationName", applicationName);
        propertiesMap.put("buildTimestamp", buildTimestamp);
        propertiesMap.put("appEncoding", appEncoding);
        propertiesMap.put("appJavaVersion", appJavaVersion);

        return ResponseEntity.ok(propertiesMap);
    }
}
