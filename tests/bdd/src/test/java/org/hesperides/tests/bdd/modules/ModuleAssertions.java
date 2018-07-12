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
package org.hesperides.tests.bdd.modules;

import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.tests.bdd.technos.TechnoAssertions;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ModuleAssertions {

    public static void assertModuleAgainstDefaultValues(ModuleIO actualModule, long expectedVersionId) {
        ModuleIO expectedModule = ModuleSamples.getModuleInputWithDefaultValues();
        assertModule(expectedModule, actualModule, expectedVersionId);
    }

    public static void assertModule(ModuleIO expectedModule, ModuleIO actualModule, long expectedVersionId) {
        assertEquals(expectedModule.getName(), actualModule.getName());
        assertEquals(expectedModule.getVersion(), actualModule.getVersion());
        assertEquals(expectedModule.isWorkingCopy(), actualModule.isWorkingCopy());
        assertTechnos(expectedModule.getTechnos(), actualModule.getTechnos());
        assertEquals(expectedVersionId, actualModule.getVersionId().longValue());
    }

    private static void assertTechnos(List<TechnoIO> expectedTechnos, List<TechnoIO> actualTechnos) {
        if (expectedTechnos != null && actualTechnos != null) {
            assertEquals(expectedTechnos.size(), actualTechnos.size());
            for (int i = 0; i < expectedTechnos.size(); i++) {
                TechnoAssertions.assertTechno(expectedTechnos.get(i), actualTechnos.get(i));
            }
        }
    }
}
