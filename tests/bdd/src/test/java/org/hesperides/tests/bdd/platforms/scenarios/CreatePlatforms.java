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
package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.IterablePropertyItemIO;
import org.hesperides.core.presentation.io.platforms.properties.IterableValuedPropertyIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.platforms.PlatformBuilder;
import org.hesperides.tests.bdd.platforms.PlatformClient;
import org.hesperides.tests.bdd.templatecontainers.builders.ModelBuilder;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.hesperides.tests.bdd.commons.StepHelper.assertOK;
import static org.hesperides.tests.bdd.commons.StepHelper.getResponseType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CreatePlatforms implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModelBuilder modelBuilder;

    private ResponseEntity responseEntity;

    public CreatePlatforms() {

        Given("^an existing platform(?: named \"([^\"]*)\")?( with this module)?( (?:and|with) an instance)?( (?:and|with) valued properties)?( (?:and|with) iterable properties)?( (?:and|with) global properties)?( (?:and|with) instance properties)?$", (
                final String platformName, final String withThisModule, final String withAnInstance, final String withValuedProperties, final String withIterableProperties, final String withGlobalProperties, final String withInstanceProperties) -> {

            if (StringUtils.isNotEmpty(platformName)) {
                platformBuilder.withPlatformName(platformName);
            }

            if (StringUtils.isNotEmpty(withThisModule)) {
                if (StringUtils.isNotEmpty(withAnInstance)) {
                    platformBuilder.withInstance("instance-foo-1");
                }
                platformBuilder.withModule(moduleBuilder.build(), moduleBuilder.getPropertiesPath());
            }
            platformClient.create(platformBuilder.buildInput());

            if (StringUtils.isNotEmpty(withValuedProperties)) {
                platformBuilder.withProperty("module-foo", "12");
                platformBuilder.withProperty("techno-foo", "12");
                platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.buildPropertiesInput(false), moduleBuilder.getPropertiesPath());
                platformBuilder.incrementVersionId();
            }

            if (StringUtils.isNotEmpty(withIterableProperties)) {
                platformBuilder.withIterableProperties(Arrays.asList(
                        new IterableValuedPropertyIO("module-foo", Arrays.asList(
                                new IterablePropertyItemIO("bloc-module-1", Arrays.asList(new ValuedPropertyIO("module-bar", "module-bar-val-1"))),
                                new IterablePropertyItemIO("bloc-module-2", Arrays.asList(new ValuedPropertyIO("module-bar", "module-bar-val-2")))
                        )),
                        new IterableValuedPropertyIO("techno-foo", Arrays.asList(
                                new IterablePropertyItemIO("bloc-techno-1", Arrays.asList(new ValuedPropertyIO("techno-bar", "techno-bar-val-1"))),
                                new IterablePropertyItemIO("bloc-techno-2", Arrays.asList(new ValuedPropertyIO("techno-bar", "techno-bar-val-2")))
                        ))
                ));
                platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.buildPropertiesInput(false), moduleBuilder.getPropertiesPath());
                platformBuilder.incrementVersionId();
            }

            if (StringUtils.isNotEmpty(withGlobalProperties)) {
                platformBuilder.withGlobalProperty("global-module-foo", "12", modelBuilder);
                platformBuilder.withGlobalProperty("global-techno-foo", "12", modelBuilder);
                platformBuilder.withGlobalProperty("unused-global-property", "12", modelBuilder);
                platformClient.saveGlobalProperties(platformBuilder.buildInput(), platformBuilder.buildPropertiesInput(true));
                platformBuilder.incrementVersionId();
            }

            if (StringUtils.isNotEmpty(withInstanceProperties)) {
                platformBuilder.withInstanceProperty("module-foo", "instance-module-foo");
                platformBuilder.withInstanceProperty("techno-foo", "instance-techno-foo");
                platformClient.saveProperties(platformBuilder.buildInput(), platformBuilder.buildPropertiesInput(false), moduleBuilder.getPropertiesPath());
                platformBuilder.incrementVersionId();
            }

            platformBuilder.addPlatform(platformBuilder.buildInput());
        });

        Given("^a platform to create(?:, named \"([^\"]*)\")?$", (final String name) -> {
            if (StringUtils.isNotEmpty(name)) {
                platformBuilder.withPlatformName(name);
            }
        });

        When("^I( try to)? create this platform$", (final String tryTo) -> {
            responseEntity = platformClient.create(platformBuilder.buildInput(), getResponseType(tryTo, PlatformIO.class));
        });

        Then("^the platform is successfully created$", () -> {
            assertOK(responseEntity);
            PlatformIO expectedPlatform = platformBuilder.buildOutput();
            PlatformIO actualPlatform = (PlatformIO) responseEntity.getBody();
            Assert.assertEquals(expectedPlatform, actualPlatform);
        });

        Then("^a ([45][0-9][0-9]) error is returned, blaming \"([^\"]+)\"$", (Integer httpCode, String message) -> {
            assertEquals(HttpStatus.valueOf(httpCode), responseEntity.getStatusCode());
            assertThat((String) responseEntity.getBody(), containsString(message));
        });
    }
}
