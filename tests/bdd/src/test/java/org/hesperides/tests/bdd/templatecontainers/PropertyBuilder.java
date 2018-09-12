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
package org.hesperides.tests.bdd.templatecontainers;

public class PropertyBuilder {

    private String name = "foo";
    private boolean isRequired;
    private String comment;
    private String defaultValue;
    private String pattern;
    private boolean isPassword;

    public PropertyBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public PropertyBuilder withIsRequired() {
        this.isRequired = true;
        return this;
    }

    public PropertyBuilder withComment(final String comment) {
        this.comment = comment;
        return this;
    }

    public PropertyBuilder withDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public PropertyBuilder withPattern(final String pattern) {
        this.pattern = pattern;
        return this;
    }

    public PropertyBuilder withIsPassword() {
        this.isPassword = true;
        return this;
    }

    public String build() {
        StringBuilder property = new StringBuilder();
        property.append("{{");
        property.append(name);
        property.append("|");
        if (isRequired) {
            property.append(" @required");
        }
        if (comment != null) {
            property.append(" @comment " + comment);
        }
        if (defaultValue != null) {
            property.append(" @default " + defaultValue);
        }
        if (pattern != null) {
            property.append(" @pattern " + pattern);
        }
        if (isPassword) {
            property.append(" @password");
        }
        property.append("}}");
        return property.toString();
    }
}
