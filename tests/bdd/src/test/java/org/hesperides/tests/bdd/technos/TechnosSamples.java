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
package org.hesperides.tests.bdd.technos;

import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.presentation.io.TechnoIO;

public class TechnosSamples {

    private static final String DEFAULT_NAME = "test-techno";
    private static final String DEFAULT_VERSION = "1.0.0";

    public static TechnoIO getTechnoFromTechnoKey(TemplateContainer.Key technoKey) {
        return new TechnoIO(technoKey.getName(), technoKey.getVersion(), technoKey.isWorkingCopy());
    }

    public static TechnoIO getTechnoWithDefaultValues() {
        return getTechnoWithNameAndVersion(DEFAULT_NAME, DEFAULT_VERSION);
    }

    public static TechnoIO getTechnoWithNameAndVersion(String name, String version) {
        return new TechnoIO(name, version, true);
    }
}
