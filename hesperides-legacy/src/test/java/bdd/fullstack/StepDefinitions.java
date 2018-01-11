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
package bdd.fullstack;

import bdd.Conf;
import bdd.CucumberTest;
import bdd.Hooks;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.vsct.dt.hesperides.api.ModuleApi;
import com.vsct.dt.hesperides.domain.modules.ModuleSearchRepository;
import com.vsct.dt.hesperides.infrastructure.redis.RedisConfiguration;
import com.vsct.dt.hesperides.infrastructure.redis.RedisModuleSearchRepository;
import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import redis.clients.jedis.Jedis;
import redis.embedded.RedisServer;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class StepDefinitions implements En {

    private void insertModule() {
        Hooks.jedis.rpush("module-foo", "bar");
    }

    private Set<String> modules = null;

    public StepDefinitions() {
        Given("^There is at least one existing module$", () -> {
            insertModule();
        });
        Given("^There is no modules$", () -> {
        });
        When("^a user retrieves the module's list$", () -> {
            modules = CucumberTest.resources.getJerseyTest().target("/toto").request().get(Set.class);
        });
        Then("^he should get the modules' list$", () -> {
            assertThat(modules.size()).isGreaterThan(0);
        });
        Then("^he should get an empty list$", () -> {
            assertThat(modules.size()).isEqualTo(0);
        });
    }
}
