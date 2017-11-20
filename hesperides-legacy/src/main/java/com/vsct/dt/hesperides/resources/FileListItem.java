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

package com.vsct.dt.hesperides.resources;

import java.util.Objects;

/**
 * Created by william_montaz on 18/08/14.
 */
public final class FileListItem {

    private String location;
    private String url;
    private FileListItemRights rights;

    private FileListItem() {
        //Jackson
    }

    public FileListItem(final String location, final String url, final FileListItemRights rights) {
        this.location = location;
        this.url = url;
        this.rights = rights;
    }

    public FileListItem(final String location, final String url) {
        this.location = location;
        this.url = url;
    }

    public String getLocation() {
        return location;
    }

    public String getUrl() {
        return url;
    }

    public FileListItemRights getRights() {
        return rights;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, url, rights);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final FileListItem other = (FileListItem) obj;
        return Objects.equals(this.location, other.location)
                && Objects.equals(this.url, other.url)
                && Objects.equals(this.rights, other.rights);
    }
}
