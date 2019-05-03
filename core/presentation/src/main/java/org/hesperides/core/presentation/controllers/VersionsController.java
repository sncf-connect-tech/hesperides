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
package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "8. Users and versions", description = " ")
@RequestMapping("/versions")
@RestController
public class VersionsController extends AbstractController {

    @Value("${application.version}")
    private String version;

    @Value("${application.build.time}")
    private String buildTime;

    @ApiOperation("Get backend API version")
    @GetMapping
    public ResponseEntity<Map<String, String>> getVersions() {

        Map<String, String> propertiesMap = new HashMap<>();
        propertiesMap.put("version", version);
        propertiesMap.put("build_time", buildTime);

        return ResponseEntity.ok(propertiesMap);
    }
}
