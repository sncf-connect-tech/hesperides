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

package com.vsct.dt.hesperides.applications.properties;

/**
 * Created by emeric_martineau on 19/01/2016.
 */
public class PropertiesTimelineKey {
    /**
     * Property key.
     */
    private final PropertiesKey key;

    /**
     * Timestamp.
     */
    private final long timestamp;

    /**
     * Constructor.
     *
     * @param key key
     * @param timestamp timestamp
     */
    public PropertiesTimelineKey(final PropertiesKey key, final long timestamp) {
        this.key = key;
        this.timestamp = timestamp;
    }

    public PropertiesKey getKey() {
        return key;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertiesTimelineKey)) return false;

        PropertiesTimelineKey that = (PropertiesTimelineKey) o;

        if (timestamp != that.timestamp) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}
