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
package org.hesperides.tests.bddrefacto.technos;

import org.hesperides.core.presentation.io.TechnoIO;
import org.springframework.stereotype.Component;

@Component
public class TechnoBuilder {

    private String name;
    private String version;
    private boolean isWorkingCopy;

    public TechnoBuilder() {
        reset();
    }

    public void reset() {
        // Valeurs par défaut
        name = "test-techno";
        version = "1.0.0";
        isWorkingCopy = true;
    }

    public TechnoBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public TechnoBuilder withVersion(final String version) {
        this.version = version;
        return this;
    }

    public TechnoBuilder withIsWorkingCopy(final boolean isWorkingCopy) {
        this.isWorkingCopy = isWorkingCopy;
        return this;
    }

    public TechnoIO build() {
        return new TechnoIO(name, version, isWorkingCopy);
    }

    public String getNamespace() {
        return "packages#" + name + "#" + version + "#" + (isWorkingCopy ? "WORKINGCOPY" : "RELEASE");
    }
}
