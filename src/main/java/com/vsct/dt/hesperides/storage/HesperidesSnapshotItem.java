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
