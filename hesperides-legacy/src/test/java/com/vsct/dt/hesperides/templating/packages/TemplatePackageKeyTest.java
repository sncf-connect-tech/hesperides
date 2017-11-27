/*
 *
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
 *
 */

package com.vsct.dt.hesperides.templating.packages;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import tests.type.UnitTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

import static com.vsct.dt.hesperides.TestUtils.flattenJSON;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by william_montaz on 29/08/14.
 */
@Category(UnitTests.class)
public class TemplatePackageKeyTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void serializesToJSON() throws IOException {
        final TemplatePackageKey templatePackage = new TemplatePackageKey("name", "version", true);

        assertThat(MAPPER.writeValueAsString(templatePackage)).isEqualTo(flattenJSON("fixtures/business/template_package.json"));

    }

    @Test
    public void shouldDeserializeFromJSON() throws IOException {
        final TemplatePackageKey templatePackage = new TemplatePackageKey("name", "version", true);

        assertThat(MAPPER.readValue(fixture("fixtures/business/template_package.json"), TemplatePackageKey.class)).isEqualTo(templatePackage);
    }
}
