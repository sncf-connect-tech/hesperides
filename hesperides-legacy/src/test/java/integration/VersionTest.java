/*
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package integration;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import tests.type.IntegrationTests;

import com.vsct.dt.hesperides.resources.HesperidesVersionsResource.Versions;

/**
 * Created by emeric_martineau on 10/03/2017.
 */
@Category(IntegrationTests.class)
public class VersionTest extends AbstractIntegrationTest {
    @Test
    public void displayVersion() {
        final Versions hesperidesVersion = hesClient.getVersion();

        System.out.println("Hesperides informations :");
        System.out.println(" - back : " + hesperidesVersion.getBackendVersion());
        System.out.println(" - api : " + hesperidesVersion.getApiVersion());
        System.out.println();
    }
}
