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
package org.hesperides.tests.bdd.platforms.samples;

import org.hesperides.core.presentation.io.platforms.InstanceIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;

import java.util.Arrays;

public class InstanceSamples {

    public static final String DEFAULT_NAME = "instance_name";
    public static final String DEFAULT_PROPERTY_NAME1= "foo";
    public static final String DEFAULT_PROPERTY_VALUE1= "bar";
    public static final String DEFAULT_PROPERTY_NAME2= "toto";
    public static final String DEFAULT_PROPERTY_VALUE2= "tata";

    public static InstanceIO getInstanceIOWithDefaultValues() {
        return new InstanceIO(
                DEFAULT_NAME,
                Arrays.asList(
                        new ValuedPropertyIO(DEFAULT_PROPERTY_NAME1, DEFAULT_PROPERTY_VALUE1),
                        new ValuedPropertyIO(DEFAULT_PROPERTY_NAME2, DEFAULT_PROPERTY_VALUE2)
                )
        );
    }
}
