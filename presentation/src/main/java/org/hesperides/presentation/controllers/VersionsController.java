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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api("/versions")
@RequestMapping("/versions")
@RestController
public class VersionsController extends AbstractController {

    @Value("${application.name}")
    private String applicationName;

    @Value("${application.build.version}")
    private String buildVersion;

    @Value("${application.build.timestamp}")
    private String buildTimestamp;

    @Value("${application.encoding}")
    private String applicationEncoding;

    @Value("${application.java.version}")
    private String javaVersion;

    @ApiOperation("Get backend and API versions")
    @GetMapping
    public ResponseEntity<Map<String, String>> getVersions() {

        Map<String, String> propertiesMap = new HashMap<>();
        propertiesMap.put("backend_version", buildVersion);
        propertiesMap.put("api_version", buildVersion);
        propertiesMap.put("application_name", applicationName);
        propertiesMap.put("build_version", buildVersion);
        propertiesMap.put("build_timestamp", buildTimestamp);
        propertiesMap.put("application_encoding", applicationEncoding);
        propertiesMap.put("java_version", javaVersion);

        return ResponseEntity.ok(propertiesMap);
    }
}
