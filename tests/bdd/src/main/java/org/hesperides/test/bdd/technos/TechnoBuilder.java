/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/sncf-connect-tech/hesperides)
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
package org.hesperides.test.bdd.technos;

import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.test.bdd.templatecontainers.TestVersionType;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateContainerBuilder;
import org.springframework.stereotype.Component;

@Component
public class TechnoBuilder extends TemplateContainerBuilder {

    public TechnoBuilder() {
        reset();
    }

    public void reset() {
        reset("test-techno");
    }

    public TechnoIO build() {
        return buildWithName(name);
    }

    public TechnoIO buildWithName(String name) {
        return new TechnoIO(name, version, TestVersionType.toIsWorkingCopy(versionType));
    }

    public String buildNamespace() {
        return "packages#" + name + "#" + version + "#" + versionType.toUpperCase();
    }
}
