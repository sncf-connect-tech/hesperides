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
import org.hesperides.domain.security.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api("/users")
@RequestMapping("/users")
@RestController
public class UsersController extends AbstractController {

    @ApiOperation("Authenticates users. It returns useful information about the authenticated user.")
    @GetMapping("/auth")
    public ResponseEntity<Map> getUserInfo(Authentication authentication) {

        User currentUser = User.fromAuthentication(authentication);

        Map userInfo = new HashMap<>();
        userInfo.put("username", currentUser.getName());
        userInfo.put("prodUser", currentUser.isProd());
        userInfo.put("techUser", currentUser.isTech());

        return ResponseEntity.ok(userInfo);
    }
}
