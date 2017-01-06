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

package com.vsct.dt.hesperides.applications;

import java.util.Objects;

/**
* Created by william_montaz on 29/04/2015.
*/
public class PlatformSnapshotKey implements SnapshotKey {

    private final long        timestamp;
    private final PlatformKey platformKey;

    public PlatformSnapshotKey(long timestamp, PlatformKey platformKey) {
        this.timestamp = timestamp;
        this.platformKey = platformKey;
    }

    @Override
    public String getIdentifier() {
        return "snapshot-platform-"+platformKey.getApplicationName()+"-"+platformKey.getName()+"-"+timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, platformKey);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PlatformSnapshotKey other = (PlatformSnapshotKey) obj;
        return Objects.equals(this.timestamp, other.timestamp)
                && Objects.equals(this.platformKey, other.platformKey);
    }

}
