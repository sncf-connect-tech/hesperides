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
package org.hesperides.tests.bddrefacto.templatecontainers;

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.springframework.stereotype.Component;

@Component
public class TemplateBuilder {

    public static final String DEFAULT_NAME = "template";

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
        rights = new RightsBuilder().build();
        versionId = 0;
        return this;
    }

    public TemplateBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public TemplateBuilder withNamespace(final String namespace) {
        this.namespace = namespace;
        return this;
    }

    public TemplateBuilder withFilename(final String filename) {
        this.filename = filename;
        return this;
    }

    public TemplateBuilder withLocation(final String location) {
        this.location = location;
        return this;
    }

    public TemplateBuilder withContent(final String content) {
        if (StringUtils.isBlank(this.content)) {
            this.content = content;
        } else {
            this.content += "\n" + content;
        }
        return this;
    }

    public TemplateBuilder withDefaultAndRequiredProperty() {
        String property = "{{defaultAndRequired|@default 12 @required}}";
        return withContent(property);
    }

    public TemplateBuilder withRights(final TemplateIO.RightsIO rights) {
        this.rights = rights;
        return this;
    }

    public TemplateBuilder withVersionId(final long versionId) {
        this.versionId = versionId;
        return this;
    }

    public TemplateIO build() {
        return new TemplateIO(name, namespace, filename, location, content, rights, versionId);
    }

    public PartialTemplateIO buildPartialTemplate(String namespace) {
        return new PartialTemplateIO(name, namespace, filename, location);
    }

    public static class RightsBuilder {

        private TemplateIO.FileRightsIO user = new FileRightsBuilder().build();
        private TemplateIO.FileRightsIO group = new FileRightsBuilder().build();
        private TemplateIO.FileRightsIO other = new FileRightsBuilder().build();

        public RightsBuilder withUser(final TemplateIO.FileRightsIO user) {
            this.user = user;
            return this;
        }

        public RightsBuilder withGroup(final TemplateIO.FileRightsIO group) {
            this.group = group;
            return this;
        }

        public RightsBuilder withOther(final TemplateIO.FileRightsIO other) {
            this.other = other;
            return this;
        }

        public TemplateIO.RightsIO build() {
            return new TemplateIO.RightsIO(user, group, other);
        }
    }

    public static class FileRightsBuilder {

        private Boolean read = null;
        private Boolean write = null;
        private Boolean execute = null;

        public FileRightsBuilder withRead(final Boolean read) {
            this.read = read;
            return this;
        }

        public FileRightsBuilder withWrite(final Boolean write) {
            this.write = write;
            return this;
        }

        public FileRightsBuilder withExecute(final Boolean execute) {
            this.execute = execute;
            return this;
        }

        public TemplateIO.FileRightsIO build() {
            return new TemplateIO.FileRightsIO(read, write, execute);
        }
    }
}
