package com.vsct.dt.hesperides.applications;

import java.util.Optional;
import java.util.Set;

/**
 * Created by emeric_martineau on 27/05/2016.
 */
public interface SnapshotRegistryInterface {
    void createSnapshot(SnapshotKey key, Object snapshot);

    Set<String> getKeys(String pattern);

    <U> Optional<U> getSnapshot(SnapshotKey snapshotKey, Class snapshotClass);
}
