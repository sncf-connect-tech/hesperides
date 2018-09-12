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
package org.hesperides.tests.bdd.templatecontainers;

import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;

import static org.junit.Assert.assertEquals;

public class TemplateAssertions {

    public static void assertTemplateAgainstDefaultValues(TemplateIO actualTemplate, String expectedNamespace, long expectedVersionId) {
        TemplateIO expectedTemplate = new TemplateBuilder().build();
        assertTemplate(expectedTemplate, actualTemplate, expectedNamespace, expectedVersionId);
    }

    public static void assertTemplate(TemplateIO expectedTemplate, TemplateIO actualTemplate, String expectedNamespace, long expectedVersionId) {
        assertEquals(expectedNamespace, actualTemplate.getNamespace());
        assertEquals(expectedTemplate.getName(), actualTemplate.getName());
        assertEquals(expectedTemplate.getFilename(), actualTemplate.getFilename());
        assertEquals(expectedTemplate.getLocation(), actualTemplate.getLocation());
        assertEquals(expectedTemplate.getContent(), actualTemplate.getContent());
        assertRights(expectedTemplate.getRights(), actualTemplate.getRights());
        assertEquals(expectedVersionId, actualTemplate.getVersionId().longValue());
    }

    public static void assertRights(TemplateIO.RightsIO expectedRights, TemplateIO.RightsIO actualRights) {
        assertEquals(expectedRights.getUser().getRead(), actualRights.getUser().getRead());
        assertEquals(expectedRights.getUser().getWrite(), actualRights.getUser().getWrite());
        assertEquals(expectedRights.getUser().getExecute(), actualRights.getUser().getExecute());
        assertEquals(expectedRights.getGroup().getRead(), actualRights.getGroup().getRead());
        assertEquals(expectedRights.getGroup().getWrite(), actualRights.getGroup().getWrite());
        assertEquals(expectedRights.getGroup().getExecute(), actualRights.getGroup().getExecute());
        assertEquals(expectedRights.getOther().getRead(), actualRights.getOther().getRead());
        assertEquals(expectedRights.getOther().getWrite(), actualRights.getOther().getWrite());
        assertEquals(expectedRights.getOther().getExecute(), actualRights.getOther().getExecute());
    }
}
