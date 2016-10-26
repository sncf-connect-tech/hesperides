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
