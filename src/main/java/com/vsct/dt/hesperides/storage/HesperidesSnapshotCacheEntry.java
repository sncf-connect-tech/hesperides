/*
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
 */

package com.vsct.dt.hesperides.storage;

import com.fasterxml.jackson.annotation.*;

/**
 * Created by emeric_martineau on 20/04/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"cacheType", "nbEvents", "data"})
public class HesperidesSnapshotCacheEntry {

    private final String cacheType;
    private final String data;
    private final long nbEvents;

    @JsonCreator
    public HesperidesSnapshotCacheEntry(@JsonProperty("cacheType") final String cacheType,
                 @JsonProperty("data") final String data, @JsonProperty("nbEvents") final long nbEvents) {
        this.cacheType = cacheType;
        this.data = data;
        this.nbEvents = nbEvents;
    }

    @JsonProperty(value = "cacheType")
    public String getCacheType() {
        return cacheType;
    }

    @JsonProperty(value = "data")
    public String getData() {
        return data;
    }

    @JsonProperty(value = "nbEvents")
    public long getNbEvents() {
        return nbEvents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HesperidesSnapshotCacheEntry)) return false;

        HesperidesSnapshotCacheEntry that = (HesperidesSnapshotCacheEntry) o;

        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        if (cacheType != null ? !cacheType.equals(that.cacheType) : that.cacheType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = cacheType != null ? cacheType.hashCode() : 0;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }
}
