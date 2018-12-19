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
package org.hesperides.tests.bdd.modules;

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.hesperides.core.presentation.io.ModuleIO.WORKINGCOPY;

@Component
public class ModuleBuilder {

    private String name;
    private String version;
    private String versionType;
    private List<TechnoIO> technos;
    private long versionId;

    private List<TemplateIO> templates;

    public ModuleBuilder() {
        reset();
    }

    public ModuleBuilder reset() {
        // Valeurs par défaut
        name = "test-module";
        version = "1.0.0";
        versionType = WORKINGCOPY;
        technos = new ArrayList<>();
        templates = new ArrayList<>();
        versionId = 0;
        return this;
    }

    public ModuleBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ModuleBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public ModuleBuilder withModuleType(String versionType) {
        this.versionType = versionType;
        return this;
    }

    public ModuleBuilder withTechno(TechnoIO techno) {
        technos.add(techno);
        return this;
    }

    public boolean hasTechno() {
        return technos.size() > 0;
    }

    public ModuleBuilder withVersionId(long versionId) {
        this.versionId = versionId;
        return this;
    }

    public ModuleIO build() {
        return new ModuleIO(name, version, isWorkingCopy(), technos, versionId);
    }

    public String getNamespace() {
        return "modules#" + name + "#" + version + "#" + versionType.toUpperCase();
    }

    public void removeTechno(TechnoIO techno) {
        if (technos.contains(techno)) {
            technos.remove(techno);
        }
    }

    public String getPropertiesPath() {
        return getPropertiesPath(null);
    }

    public String getPropertiesPath(String logicalGroup) {
        return "#" + StringUtils.defaultString(logicalGroup, "GROUP") + "#" + name + "#" + version + "#" + versionType.toUpperCase();
    }

    public String getVersionType() {
        return versionType;
    }

    public Boolean isWorkingCopy() {
        return versionType == WORKINGCOPY;
    }

    public ModuleBuilder withTemplate(TemplateIO template) {
        templates.add(template);
        return this;
    }

    public List<TemplateIO> getTemplates() {
        return templates;
    }
}
