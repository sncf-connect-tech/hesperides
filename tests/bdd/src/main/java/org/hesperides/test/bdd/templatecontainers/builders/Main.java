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
package org.hesperides.test.bdd.templatecontainers.builders;

import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class Main {

    @Test
    public void test() {
        Assert.assertEquals("bar", PropertyBuilder.replacePropertiesWithValues("{{foo}}", Collections.emptyList(), Collections.singletonList(new ValuedPropertyIO("foo", "bar"))));
        Assert.assertEquals("bar", PropertyBuilder.replacePropertiesWithValues("{{ foo }}", Collections.emptyList(), Collections.singletonList(new ValuedPropertyIO("foo", "bar"))));
        Assert.assertEquals("foo bar", PropertyBuilder.replacePropertiesWithValues("{{ foo }} {{ bar }}", Collections.emptyList(), Arrays.asList(new ValuedPropertyIO("foo", "foo"), new ValuedPropertyIO("bar", "bar"))));
    }
}
