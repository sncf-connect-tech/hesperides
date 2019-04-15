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
package org.hesperides.core.domain.versions;

import org.junit.Test;

import static org.hesperides.core.domain.versions.SemVerComparator.semVerCompare;
import static org.junit.Assert.assertEquals;

public class SemVerComparatorTest {

    @Test
    public void test_semVerCompare_equal() {
        assertEquals(semVerCompare("", ""), 0);
        assertEquals(semVerCompare("0", "0"), 0);
        assertEquals(semVerCompare("0.0", "0.0"), 0);
        assertEquals(semVerCompare("0.a", "0.a"), 0);
    }

    @Test
    public void test_semVerCompare_singleDigit() {
        assertEquals(semVerCompare("2", "1"), 1);
    }

    @Test
    public void test_semVerCompare_doubleDigits() {
        assertEquals(semVerCompare("1.12", "1.2"), 10);
    }

    @Test
    public void test_semVerCompare_tmp() {
        assertEquals(semVerCompare("1.12", "1.2"), 10);
    }

    @Test
    public void test_semVerCompare_nonNumeric() {
        assertEquals(semVerCompare("b", "a"), 1);
    }

    @Test
    public void test_semVerCompare_shorterChain() {
        assertEquals(semVerCompare("1.2", "1"), 1);
    }
}
