/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides.templating.modules.template;

import com.google.common.base.Strings;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by william_montaz on 19/02/2015.
 */
public class TemplateData {

    private String name;
    private String filename;
    private String location;
    private String content;
    private long versionID = 1L; //Default value is the first versionID given to an object
    private TemplateRights rights;

    private TemplateData() {
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public String getLocation() {
        return location;
    }

    public String getContent() {
        return content;
    }

    public long getVersionID() {
        return versionID;
    }

    public TemplateRights getRights() {
        return rights;
    }

    @Override
    public String toString() {
        return "TemplateData{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, filename, location, content, versionID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TemplateData other = (TemplateData) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.filename, other.filename)
                && Objects.equals(this.location, other.location)
                && Objects.equals(this.content, other.content)
                && Objects.equals(this.versionID, other.versionID);
    }

    /**
     * Builder methods
     */

    public static IFilename withTemplateName(String name) {
        return new Builder(name);
    }

    public static interface IFilename {
        public ILocation withFilename(String filename);
    }

    public static interface ILocation {
        public IContent withLocation(String location);
    }

    public static interface IContent {
        public IRights withContent(String content);
    }

    public static interface IBuild {
        public IBuild withVersionID(long versionID);

        public TemplateData build();
    }

    public static interface IRights {
        public IBuild withRights(TemplateRights rights);
    }

    public static class Builder implements IFilename, ILocation, IContent, IBuild, IRights {

        private TemplateData instance = new TemplateData();

        public Builder(String name) {
            checkArgument(!Strings.isNullOrEmpty(name), "Template name should not be empty or null");
            instance.name = name;
        }

        @Override
        public IBuild withVersionID(long versionID) {
            checkArgument(versionID >= 1, "Version ID should be greater are equal to 1");
            instance.versionID = versionID;
            return this;
        }

        @Override
        public TemplateData build() {
            return instance;
        }

        @Override
        public IRights withContent(String content) {
            checkNotNull(content, "Template content cannot be null");
            instance.content = content;
            return this;
        }

        @Override
        public ILocation withFilename(String filename) {
            checkArgument(!Strings.isNullOrEmpty(filename), "Template filename should not be empty or null");
            instance.filename = filename;
            return this;
        }

        @Override
        public IContent withLocation(String location) {
            checkArgument(!Strings.isNullOrEmpty(location), "Template location should not be empty or null");
            instance.location = location;
            return this;
        }

        @Override
        public IBuild withRights(TemplateRights rights) {
            if (rights == null) {
                TemplateFileRights tfr = new TemplateFileRights(null, null, null);
                rights = new TemplateRights(tfr, tfr, tfr);
            }
            instance.rights = rights;
            return this;
        }
    }
}
