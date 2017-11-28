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

package com.vsct.dt.hesperides.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by william_montaz on 23/01/2015.
 */
public class VersionTest {

    @Test
    public void should_say_release_is_greater_than_working_copy() {
        Version v1 = new Version("1.0.0.0", false);
        Version v2 = new Version("1.0.0.0", true);
        assertThat(v1.isGreaterThan(v2)).isTrue();
        assertThat(v2.isGreaterThan(v1)).isFalse();

        v1 = new Version("1.0.1", false);
        v2 = new Version("1.0.1.0", true);

        assertThat(v1.isGreaterThan(v2)).isTrue();
        assertThat(v2.isGreaterThan(v1)).isFalse();
    }

    @Test
    public void should_say_not_greater_when_equals() {
        Version v1 = new Version("1.0.0.0", false);
        Version v2 = new Version("1.0", false);

        assertThat(v1.isGreaterThan(v2)).isFalse();
        assertThat(v2.isGreaterThan(v1)).isFalse();

        v1 = new Version("1.0.0.0", true);
        v2 = new Version("1.0.0.0", true);

        assertThat(v1.isGreaterThan(v2)).isFalse();
        assertThat(v2.isGreaterThan(v1)).isFalse();
    }

    @Test
    public void should_say_greater_when_version_number_is_greater() {
        Version v1 = new Version("1.0.0.0", false);
        Version v2 = new Version("1.0.0", false);
        Version v3 = new Version("1.0.0.1", false);
        Version v4 = new Version("1.1", false);
        Version v5 = new Version("1.0.1.0", false);

        assertThat(v1.isGreaterThan(v3)).isFalse();

        assertThat(v3.isGreaterThan(v1)).isTrue();
        assertThat(v3.isGreaterThan(v2)).isTrue();

        assertThat(v4.isGreaterThan(v3)).isTrue();
        assertThat(v3.isGreaterThan(v4)).isFalse();

        assertThat(v4.isGreaterThan(v5)).isTrue();
        assertThat(v5.isGreaterThan(v4)).isFalse();

        assertThat(v5.isGreaterThan(v3)).isTrue();
        assertThat(v3.isGreaterThan(v5)).isFalse();
    }

}
