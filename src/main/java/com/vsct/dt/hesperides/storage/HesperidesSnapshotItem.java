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

/**
 * Object return by findSnapshot method.
 *
 * Created by emeric_martineau on 19/05/2016.
 */
public class HesperidesSnapshotItem {

    private final Object snapshot;

    /**
     * Nb event in redis stream.
     */
    private final long currentNbEvent;

    /**
     * Nb event in cache.
     */
    private final long nbEvents;

    public HesperidesSnapshotItem(final Object snapshot, final long nbEvents, final long currentNbEvent) {
        this.snapshot = snapshot;
        this.currentNbEvent = currentNbEvent;
        this.nbEvents = nbEvents;
    }

    public Object getSnapshot() {
        return snapshot;
    }

    public long getCurrentNbEvents() {
        return currentNbEvent;
    }

    public long getNbEvents() {
        return nbEvents;
    }
}
