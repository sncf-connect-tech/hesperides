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

import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.presentation.io.ModuleIO;
import org.hesperides.presentation.io.TechnoIO;
import org.hesperides.tests.bdd.technos.TechnosSamples;

import java.util.Arrays;
import java.util.Collections;

public class ModuleSamples {

    public static final String DEFAULT_NAME = "test";
    public static final String DEFAULT_VERSION = "1.0.0";
    public static final long DEFAULT_VERSION_ID = 0;

    public static ModuleIO getModuleInputWithDefaultValues() {
        return new ModuleIO(DEFAULT_NAME, DEFAULT_VERSION, true, Collections.emptyList(), DEFAULT_VERSION_ID);
    }

    public static ModuleIO getModuleInputWithTechnoAndVersionId(TemplateContainer.Key technoKey, long versionId) {
        TechnoIO technoInput = TechnosSamples.getTechnoFromTechnoKey(technoKey);
        return new ModuleIO(DEFAULT_NAME, DEFAULT_VERSION, true, Arrays.asList(technoInput), versionId);
    }

    public static ModuleIO getModuleInputWithNameAndVersion(String name, String version) {
        return new ModuleIO(name, version, true, Collections.emptyList(), DEFAULT_VERSION_ID);
    }

    public static ModuleIO getModuleInputWithVersionId(long versionId) {
        return new ModuleIO(DEFAULT_NAME, DEFAULT_VERSION, true, Collections.emptyList(), versionId);
    }

    public static ModuleIO getModuleInputWithNameAndVersionAndTechno(String name, String version, TemplateContainer.Key technoKey) {
        TechnoIO technoInput = TechnosSamples.getTechnoFromTechnoKey(technoKey);
        return new ModuleIO(name, version, true, Arrays.asList(technoInput), DEFAULT_VERSION_ID);
    }
}
