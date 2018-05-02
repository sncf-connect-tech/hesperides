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
package org.hesperides.tests.bdd.templatecontainer;

import org.hesperides.presentation.io.TemplateIO;

import static org.junit.Assert.assertEquals;

/**
 * Nommé comme ça par défaut
 * TODO Changer
 */
public class TemplateUtils {

    public static void assertRights(TemplateIO.RightsIO rightsInput, TemplateIO.RightsIO rightsOutput) {
        assertEquals(rightsInput.getUser().getRead(), rightsOutput.getUser().getRead());
        assertEquals(rightsInput.getUser().getWrite(), rightsOutput.getUser().getWrite());
        assertEquals(rightsInput.getUser().getExecute(), rightsOutput.getUser().getExecute());
        assertEquals(rightsInput.getGroup().getRead(), rightsOutput.getGroup().getRead());
        assertEquals(rightsInput.getGroup().getWrite(), rightsOutput.getGroup().getWrite());
        assertEquals(rightsInput.getGroup().getExecute(), rightsOutput.getGroup().getExecute());
        assertEquals(rightsInput.getOther().getRead(), rightsOutput.getOther().getRead());
        assertEquals(rightsInput.getOther().getWrite(), rightsOutput.getOther().getWrite());
        assertEquals(rightsInput.getOther().getExecute(), rightsOutput.getOther().getExecute());
    }
}
