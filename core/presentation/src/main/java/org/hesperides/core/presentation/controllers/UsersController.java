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
import org.hesperides.core.application.security.UserUseCases;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.presentation.io.UserInfoOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Api(tags = "10. Users and versions", description = " ")
@RequestMapping("/users")
@RestController
public class UsersController extends AbstractController {

    private final UserUseCases userUseCases;
    private final Set<String> loggedOutUsers = new HashSet<>();

    @Autowired
    public UsersController(UserUseCases userUseCases) {
        this.userUseCases = userUseCases;
    }

    @ApiOperation("Retrieve information about a known user.")
    @GetMapping("/{username}")
    public ResponseEntity<UserInfoOutput> getUserInfo(@PathVariable final String username) {
        final User user = userUseCases.getUser(username);
        return ResponseEntity.ok(new UserInfoOutput(user));
    }

    @ApiOperation("Authenticates users. It returns useful information about the authenticated user.")
    @GetMapping("/auth")
    public ResponseEntity getCurrentUserInfo(Authentication authentication,
                                             @RequestParam(value = "logout", required = false) final Boolean logout) {

        if (Boolean.TRUE.equals(logout)) {
            /*
             * Dans le cas d'une déconnexion, on veut retourner une 401 lors de la première requête,
             * mais permettre la reconnexion avec d'autre credentials lors de la seconde,
             * où le brower renverra aussi ?logout=true une fois la popup affichée à l'utilisateur.
             * On mémorise donc temporairement que cet utilisateur a été déconnecté
             * pour ne pas retourner une 401 la seconde fois.
             */
            if (loggedOutUsers.contains(authentication.getName())) {
                loggedOutUsers.remove(authentication.getName());
            } else {
                loggedOutUsers.add(authentication.getName());
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.set("WWW-Authenticate", "Basic realm=\"Realm\"");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .headers(responseHeaders)
                        .body("Performing logout");
            }
        }

        return ResponseEntity.ok(new UserInfoOutput(authentication));
    }
}
