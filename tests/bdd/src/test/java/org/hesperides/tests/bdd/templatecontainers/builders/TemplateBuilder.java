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
package org.hesperides.tests.bdd.templatecontainers.builders;

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.springframework.stereotype.Component;

@Component
public class TemplateBuilder {

    public static String DEFAULT_NAME = "template";

    private String name;
    private String namespace;
    private String filename;
    private String location;
    private String content;
    private TemplateIO.RightsIO rights;
    private long versionId;

    public TemplateBuilder() {
        reset();
    }

    public TemplateBuilder reset() {
        // Valeurs par défaut
        name = DEFAULT_NAME;
        namespace = null;
        filename = "template.json";
        location = "/location";
        content = "content";
        rights = defaultRights();
        versionId = 0;
        return this;
    }

    private TemplateIO.RightsIO defaultRights() {
        return new TemplateIO.RightsIO(
                new TemplateIO.FileRightsIO(true, true, true),
                new TemplateIO.FileRightsIO(false, false, false),
                new TemplateIO.FileRightsIO(null, null, null));
    }

    public TemplateBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TemplateBuilder withNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public TemplateBuilder withFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public TemplateBuilder withLocation(String location) {
        this.location = location;
        return this;
    }

    public TemplateBuilder withContent(String content) {
        if (StringUtils.isBlank(this.content)) {
            this.content = content;
        } else {
            this.content += "\n" + content;
        }
        return this;
    }

    public TemplateBuilder withVersionId(long versionId) {
        this.versionId = versionId;
        return this;
    }

    public TemplateIO build() {
        return new TemplateIO(name, namespace, filename, location, content, rights, versionId);
    }

    public PartialTemplateIO buildPartialTemplate(String namespace) {
        return new PartialTemplateIO(name, namespace, filename, location);
    }
}
