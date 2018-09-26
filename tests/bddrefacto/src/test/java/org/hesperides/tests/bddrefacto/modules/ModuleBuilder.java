/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-modulelogies/hesperides)
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
package org.hesperides.tests.bddrefacto.modules;

import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.TechnoIO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ModuleBuilder {

    private String name;
    private String version;
    private boolean isWorkingCopy;
    private List<TechnoIO> technos;
    private long versionId;

    public ModuleBuilder() {
        reset();
    }

    public ModuleBuilder reset() {
        // Valeurs par défaut
        name = "test-module";
        version = "1.0.0";
        isWorkingCopy = true;
        technos = new ArrayList<>();
        versionId = 0;
        return this;
    }

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

    public ModuleBuilder withTechno(final TechnoIO techno) {
        technos.add(techno);
        return this;
    }

    public ModuleBuilder withVersionId(final long versionId) {
        this.versionId = versionId;
        return this;
    }

    public ModuleIO build() {
        return new ModuleIO(name, version, isWorkingCopy, technos, versionId);
    }

    public String getNamespace() {
        return "modules#" + name + "#" + version + "#" + (isWorkingCopy ? "WORKINGCOPY" : "RELEASE");
    }

    public void removeTechno(TechnoIO techno) {
        if (technos.contains(techno)) {
            technos.remove(techno);
        }
    }
}
