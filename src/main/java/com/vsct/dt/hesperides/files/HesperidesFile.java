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

package com.vsct.dt.hesperides.files;

import com.vsct.dt.hesperides.templating.TemplateRights;

import java.util.Objects;

/**
 * Created by william_montaz on 06/01/2015.
 */
public final class HesperidesFile {

    private String templateNamespace;
    private String templateName;
    private String location;
    private String filename;
    private String content;
    private HesperidesFileRights rights;

    /**
     * Constructor for Jackson
     */
    private HesperidesFile(){

    }

    public HesperidesFile(String templateNamespace, String templateName, String location, String filename,
                          HesperidesFileRights rights) {
        this.templateNamespace = templateNamespace;
        this.templateName = templateName;
        this.location = location;
        this.filename = filename;
        this.content = "";
        this.rights = rights;
    }

    public HesperidesFile(String templateNamespace, String templateName, String location, String filename,
                          String content, HesperidesFileRights rights) {
        this.templateNamespace = templateNamespace;
        this.templateName = templateName;
        this.location = location;
        this.filename = filename;
        this.content = content;
        this.rights = rights;
    }

    public String getTemplateNamespace() {
        return templateNamespace;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getLocation() {
        return location;
    }

    public String getFilename() {
        return filename;
    }

    public String getContent() {
        return content;
    }

    public HesperidesFileRights getRights() {
        return rights;
    }

    /* Semantically equals if they have same filename and location */
    @Override
    public int hashCode() {
        return Objects.hash(location, filename);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final HesperidesFile other = (HesperidesFile) obj;
        return Objects.equals(this.location, other.location)
                && Objects.equals(this.filename, other.filename);
    }

    @Override
    public String toString() {
        return "HesperidesFile{" +
                "location='" + location + '\'' +
                ", filename='" + filename + '\'' +
                '}';
    }
}
