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
package org.hesperides.test.bdd.technos;

import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.hesperides.core.presentation.io.TechnoIO.WORKINGCOPY;

@Component
public class TechnoBuilder {

    private String name;
    private String version;
    private String versionType;
    private List<PropertyOutput> properties;

    private List<TemplateIO> templates;

    public TechnoBuilder() {
        reset();
    }

    public void reset() {
        // Valeurs par défaut
        name = "test-techno";
        version = "1.0.0";
        versionType = WORKINGCOPY;
        properties = new ArrayList<>();
        templates = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public TechnoBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TechnoBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public TechnoBuilder withModuleType(String versionType) {
        this.versionType = versionType;
        return this;
    }

    public TechnoIO build() {
        return new TechnoIO(name, version, isWorkingCopy());
    }

    public String getNamespace() {
        return "packages#" + name + "#" + version + "#" + versionType.toUpperCase();
    }

    public void withProperty(PropertyOutput property) {
        properties.add(property);
    }

    public List<PropertyOutput> getProperties() {
        return properties;
    }

    public String getVersionType() {
        return versionType;
    }

    public Boolean isWorkingCopy() {
        return versionType == WORKINGCOPY;
    }

    public TechnoBuilder withTemplate(TemplateIO template) {
        templates.add(template);
        return this;
    }

    public List<TemplateIO> getTemplates() {
        return templates;
    }
}
