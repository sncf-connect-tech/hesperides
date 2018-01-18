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
package com.vsct.dt.hesperides.bdd.api.modules;

import com.vsct.dt.hesperides.bdd.api.FullstackMockedTest;
import com.vsct.dt.hesperides.bdd.api.utils.JedisUtil;
import cucumber.api.java8.En;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class GetModulesSteps implements En {
    private JedisUtil jedisUtil;

    /**
     * Injection de dépendances => Permet de partager une ressource (ici jedisUtil) avec le Hook
     * 
     * @param jedisUtil
     */
    public GetModulesSteps(final JedisUtil jedisUtil) {
        this.jedisUtil = jedisUtil;

        Given("^There is at least one existing module$", () -> {
            insertModule();
        });
        Given("^There is no modules$", () -> {
        });
        When("^a user retrieves the module's list$", () -> {
            modules = FullstackMockedTest.resources.getJerseyTest().target("/toto").request().get(Set.class);
        });
        Then("^he should get the modules' list$", () -> {
            assertThat(modules.size()).isGreaterThan(0);
        });
        Then("^he should get an empty list$", () -> {
            assertThat(modules.size()).isEqualTo(0);
        });
    }

    private void insertModule() {
        jedisUtil.getJedis().rpush("module-foo", "bar");
    }

    private Set<String> modules = null;
}
