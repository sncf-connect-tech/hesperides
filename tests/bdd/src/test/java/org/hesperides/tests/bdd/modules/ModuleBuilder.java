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
import org.hesperides.tests.bdd.commons.tools.AbstractBuilder;

import java.util.Collections;
import java.util.List;


public class ModuleBuilder extends AbstractBuilder {

    private String name = "test";
    private String version = "1.0.0";
    private boolean isWorkingCopy = true;
    private List<TechnoIO> technos = Collections.emptyList();
    private long versionId = 0;

    public ModuleBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public ModuleBuilder withVersion(final String version) {
        this.version = version;
        return this;
    }

    public ModuleBuilder withIsWorkingCopy(final boolean isWorkingCopy) {
        this.isWorkingCopy = isWorkingCopy;
        return this;
    }

    public ModuleBuilder withTechnos(List<TechnoIO> technos) {
        this.technos = technos;
        return this;
    }

    public ModuleBuilder withTechno(TechnoIO techno) {
        this.technos = Collections.singletonList(techno);
        return this;
    }

    public ModuleBuilder withVersionId(final long versionId) {
        this.versionId = versionId;
        return this;
    }

    public ModuleIO build() {
        return new ModuleIO(name, version, isWorkingCopy, technos, versionId);
    }
}
