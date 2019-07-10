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
package org.hesperides.test.bdd.applications;

import org.hesperides.core.presentation.io.platforms.ApplicationAuthoritiesInput;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ApplicationAuthoritiesBuilder {

    private String applicationName;
    private Map<String, List<String>> authorities;

    public ApplicationAuthoritiesBuilder() {
        reset();
    }

    public ApplicationAuthoritiesBuilder reset() {
        applicationName = "test-application";
        authorities = new HashMap<>();
        return this;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void withApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void withAuthorities(List<String> authorities) {
        String authoritiesKey = applicationName + "_PROD_USER";
        if (this.authorities.containsKey(authoritiesKey)) {
            this.authorities.get(authoritiesKey).addAll(authorities);
        } else {
            this.authorities.put(applicationName + "_PROD_USER", authorities);
        }
    }

    public ApplicationAuthoritiesInput buildInput() {
        return new ApplicationAuthoritiesInput(authorities);
    }

    public Map<String, List<String>> getAuthorities() {
        return authorities;
    }
}
