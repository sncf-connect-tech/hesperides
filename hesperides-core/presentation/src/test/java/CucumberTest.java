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

import com.vsct.dt.hesperides.api.ModuleApi;
import cucumber.api.CucumberOptions;
import cucumber.api.java8.En;
import cucumber.api.junit.Cucumber;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features") //classpath:features
public class CucumberTest implements En {
    /**
     * Je laisse ça là pour utilisation ultérieure.
     * La protection des ressources est testée dans ProtectedAccessTest
     * On peut se passer de tester cet aspect dans les autres tests
     * Et donc utiliser un ResourceTestRule plus basique, comme celui-ci :
     */
    @ClassRule
    public static final ResourceTestRule RULE = ResourceTestRule.builder()
            .addResource(mock(ModuleApi.class))
            .build();
}
