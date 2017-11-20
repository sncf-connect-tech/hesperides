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

package com.vsct.dt.hesperides.applications.virtual;

import com.vsct.dt.hesperides.applications.SnapshotKey;
import com.vsct.dt.hesperides.applications.SnapshotRegistryInterface;

import java.util.Optional;
import java.util.Set;

/**
 * Created by emeric_martineau on 27/05/2016.
 */
public class VirtualSnapshotRegistry implements SnapshotRegistryInterface {
    @Override
    public void createSnapshot(SnapshotKey key, Object snapshot) {

    }

    @Override
    public Set<String> getKeys(String pattern) {
        return null;
    }

    @Override
    public <U> Optional<U> getSnapshot(SnapshotKey snapshotKey, Class snapshotClass) {
        return null;
    }
}
