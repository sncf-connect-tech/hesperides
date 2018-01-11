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
package com.vsct.dt.hesperides.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.vsct.dt.hesperides.api.ModuleApi;
import com.vsct.dt.hesperides.domain.modules.ModuleSearchRepository;
import com.vsct.dt.hesperides.infrastructure.redis.RedisConfiguration;
import com.vsct.dt.hesperides.infrastructure.redis.RedisModuleSearchRepository;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features/get-modules.feature")
public class GetModulesTest {

    private static Injector createInjector() {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                RedisConfiguration redisConfiguration = new RedisConfiguration();
                redisConfiguration.setHost(Conf.REDIS_HOST);
                redisConfiguration.setPort(Conf.REDIS_PORT);
                bind(RedisConfiguration.class).toInstance(redisConfiguration);
                bind(ModuleSearchRepository.class).to(RedisModuleSearchRepository.class);
            }
        });
    }

    private static Injector injector = createInjector();

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(injector.getInstance(ModuleApi.class))
            .build();
}
