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
package org.hesperides.core.presentation.io;

import lombok.Value;
import org.hesperides.core.domain.security.entities.User;
import org.springframework.security.core.Authentication;

import java.util.List;

@Value
public class UserInfoOutput {

    String username;
    Boolean prodUser; // déprécié, utiliser plutôt .authorities
    Boolean techUser; // déprécié, utiliser plutôt .authorities
    AuthoritiesOutput authorities;

    public UserInfoOutput(Authentication authentication) {
        User currentUser = User.fromAuthentication(authentication);
        this.username = currentUser.getName();
        this.prodUser = currentUser.isGlobalProd();
        this.techUser = currentUser.isGlobalTech();
        this.authorities = new AuthoritiesOutput(currentUser.getRoles(), currentUser.getGroups());
    }

    @Value
    public static class AuthoritiesOutput {
        List<String> roles;
        List<String> groups;
    }
}
