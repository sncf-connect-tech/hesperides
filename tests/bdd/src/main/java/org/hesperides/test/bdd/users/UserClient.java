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
package org.hesperides.test.bdd.users;

import org.hesperides.core.presentation.io.UserInfoOutput;
import org.hesperides.test.bdd.commons.CustomRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.hesperides.test.bdd.commons.TestContext.getResponseType;

@Component
public class UserClient {

    private final CustomRestTemplate restTemplate;

    @Autowired
    public UserClient(CustomRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    void getCurrentUserInfo() {
        restTemplate.getForEntity("/users/auth", UserInfoOutput.class);
    }

    void getUserInfo(String username) {
        getUserInfo(username, null);
    }

    void getUserInfo(String username, String tryTo) {
        restTemplate.getForEntity("/users/" + username, getResponseType(tryTo, UserInfoOutput.class));
    }

    void logout() {
        restTemplate.getForEntity("/users/auth?logout=true", String.class);
    }
}
