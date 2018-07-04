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

import org.hesperides.domain.security.UserRole;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collection;

@RequestMapping("/security")
@RestController
public class SecuritySamplesController extends AbstractController {
    @GetMapping("/currentUser")
    public String getPrincipal(Principal currentUser) {
        return currentUser.getName();
    }

    @GetMapping("/authentication")
    public Collection<? extends GrantedAuthority> getAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            System.out.println(authority.getAuthority());
        }
        return userDetails.getAuthorities();
    }

    @GetMapping("/prod")
    @Secured(UserRole.PROD)
    public String prod() {
        return "You are prod";
    }

    @GetMapping("/tech")
    @PreAuthorize("hasRole('" + UserRole.TECH + "')")
    public String tech() {
        return "You are tech";
    }

    @GetMapping("/prod-and-tech")
    @PreAuthorize("hasRole('" + UserRole.PROD + "') and hasRole('" + UserRole.TECH + "')")
    public String prodAndTech() {
        return "You are prod and tech";
    }

    @GetMapping("/prod-or-tech")
    @Secured({UserRole.PROD, UserRole.TECH})
    public String prodOrTech() {
        return "You are prod or tech";
    }
}
