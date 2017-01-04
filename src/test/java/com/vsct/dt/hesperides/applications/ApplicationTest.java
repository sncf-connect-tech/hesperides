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

package com.vsct.dt.hesperides.applications;

import com.cedarsoftware.util.DeepEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.vsct.dt.hesperides.resources.Application;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import java.io.IOException;

import static com.vsct.dt.hesperides.TestUtils.flattenJSON;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by william_montaz on 29/08/14.
 */
public class ApplicationTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void shouldSerializeToJSON() throws IOException {
        final Application application = new Application("uni", Lists.newArrayList());

        assertThat(MAPPER.writeValueAsString(application)).isEqualTo(flattenJSON("fixtures/business/application.json"));
    }

    @Test
    public void shouldDeserializeFromJSON() throws IOException {
        final Application application = new Application("uni", Lists.newArrayList());

        assertThat(DeepEquals.deepEquals(MAPPER.readValue(fixture("fixtures/business/application.json"), Application.class), application)).isTrue();
    }

}
