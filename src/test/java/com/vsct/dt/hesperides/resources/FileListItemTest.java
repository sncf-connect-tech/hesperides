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

package com.vsct.dt.hesperides.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import tests.type.UnitTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

import static com.vsct.dt.hesperides.TestUtils.flattenJSON;

/**
 * Created by william_montaz on 29/08/14.
 */
@Category(UnitTests.class)
public class FileListItemTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void serializesToJSON() throws IOException {
        final FileListItem fileListItem = new FileListItem("some_location", "some_url");

        assertThat(MAPPER.writeValueAsString(fileListItem)).isEqualTo(flattenJSON("fixtures/business/file_list_item.json"));
    }

    @Test
    public void equalsWithLocationAndUrl() {
        FileListItem base = new FileListItem("location", "url");
        FileListItem equals = new FileListItem("location", "url");
        FileListItem differentLocation = new FileListItem("different", "url");
        FileListItem differentUrl = new FileListItem("location", "different");

        assertThat(base).isEqualTo(equals);
        assertThat(base.hashCode()).isEqualTo(equals.hashCode());

        assertThat(base).isNotEqualTo(differentLocation);
        assertThat(base).isNotEqualTo(differentUrl);
    }

    @Test
    public void equalsAgainstSameInstance() {
        FileListItem fli = new FileListItem("location", "url");
        assertThat(fli).isEqualTo(fli);
    }

    @Test
    public void equalsAgainstNull() {
        FileListItem fli = new FileListItem("location", "url");
        assertThat(fli).isNotEqualTo(null);
    }

}
