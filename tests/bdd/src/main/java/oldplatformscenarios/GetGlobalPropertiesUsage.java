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
package oldplatformscenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.properties.GlobalPropertyUsageOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.hesperides.test.bdd.modules.OldModuleClient;
import org.hesperides.test.bdd.platforms.OldPlatformBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformClient;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GetGlobalPropertiesUsage extends HesperidesScenario implements En {

    @Autowired
    private OldPlatformClient oldPlatformClient;
    @Autowired
    private OldPlatformBuilder oldPlatformBuilder;
    @Autowired
    private OldModuleBuilder moduleBuilder;
    @Autowired
    private OldModuleClient moduleClient;
    @Autowired
    private ModelBuilder modelBuilder;

    public GetGlobalPropertiesUsage() {

        Given("^the deployed module properties are valued with the platform global properties$", () -> {
            oldPlatformBuilder.withProperty("module-foo", "{{ instance-property-a }}{{ global-module-foo }}");
            oldPlatformBuilder.withProperty("techno-foo", "{{ global-techno-foo }}");
            oldPlatformClient.saveProperties(oldPlatformBuilder.buildInput(), oldPlatformBuilder.getPropertiesIO(false), moduleBuilder.getPropertiesPath());
            oldPlatformBuilder.withGlobalProperty("global-module-foo", "whatever", true, false);
            oldPlatformBuilder.withGlobalProperty("global-techno-foo", "whatever", true, false);
        });

        Given("^the properties are removed from the module$", () -> {
            moduleClient.delete(moduleBuilder.build());
            oldPlatformBuilder.withGlobalProperty("global-module-foo", "whatever", true, true);
            oldPlatformBuilder.withGlobalProperty("global-techno-foo", "whatever", true, true);
            oldPlatformBuilder.withGlobalProperty("unused-global-property", "12", modelBuilder);
        });

        When("^I get this platform global properties usage$", () -> {
            testContext.setResponseEntity(oldPlatformClient.getGlobalPropertiesUsage(oldPlatformBuilder.buildInput()));
        });

        Then("^the platform global properties usage is successfully retrieved$", () -> {
            assertOK();

            Map<String, Set<GlobalPropertyUsageOutput>> expectedProperties = new HashMap<>();
            oldPlatformBuilder.getProperties().forEach(property -> {
                if (property.isGlobal()) {
                    Set<GlobalPropertyUsageOutput> usages = new HashSet<>();
                    if (property.isUsed()) {
                        usages.add(new GlobalPropertyUsageOutput(!property.isRemovedFromTemplate(), moduleBuilder.getPropertiesPath()));
                    }
                    expectedProperties.put(property.getName(), usages);
                }
            });

            Map<String, Set<GlobalPropertyUsageOutput>> actualProperties = testContext.getResponseBodyAsMap();
            Assert.assertEquals(expectedProperties, actualProperties);
        });
    }
}
