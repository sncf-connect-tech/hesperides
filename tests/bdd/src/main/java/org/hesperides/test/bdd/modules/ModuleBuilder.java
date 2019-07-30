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
package org.hesperides.test.bdd.modules;

import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.templatecontainers.TemplateContainerHelper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ModuleBuilder {

    private String name;
    private String version;
    private String versionType;
    private List<TechnoIO> technos;
    private long versionId;

    private List<TemplateIO> templates;
    private String logicalGroup;

    public ModuleBuilder() {
        reset();
    }

    public ModuleBuilder reset() {
        // Valeurs par défaut
        name = "test-module";
        version = "1.0.0";
        versionType = TemplateContainerHelper.WORKINGCOPY;
        technos = new ArrayList<>();
        templates = new ArrayList<>();
        versionId = 0;
        logicalGroup = null;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public ModuleBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ModuleBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public ModuleBuilder withVersionType(String versionType) {
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
        return new ModuleIO(name, version, TemplateContainerHelper.isWorkingCopy(versionType), technos, versionId);
    }

    public String getNamespace() {
        return "modules#" + name + "#" + version + "#" + versionType.toUpperCase();
    }

    public void removeTechno(TechnoIO techno) {
        technos.remove(techno);
    }

    public String getPropertiesPath() {
        String modulePath = "#GROUP";
        if ("".equals(logicalGroup) || "#".equals(logicalGroup)) {
            modulePath = logicalGroup;
        } else if (logicalGroup != null) {
            modulePath = "#" + logicalGroup;
        }
        return modulePath + "#" + name + "#" + version + "#" + versionType.toUpperCase();
    }

    public String getVersionType() {
        return versionType;
    }

    public ModuleBuilder withTemplate(TemplateIO template) {
        templates.add(template);
        return this;
    }

    public List<TemplateIO> getTemplates() {
        return templates;
    }

    public String getLogicalGroup() {
        return logicalGroup;
    }

    public void setLogicalGroup(String logicalGroup) {
        this.logicalGroup = logicalGroup;
    }
}
