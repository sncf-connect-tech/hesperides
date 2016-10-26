package com.vsct.dt.hesperides.applications;

import java.util.Objects;

/**
 * Created by emeric_martineau on 18/01/2016.
 */
public class PlatformTimelineKey {

    private final PlatformKey platformKey;
    private final long        timestamp;

    public PlatformTimelineKey(PlatformKey platformKey, long timestamp) {
        this.platformKey = platformKey;
        this.timestamp = timestamp;
    }

    public PlatformKey getPlatformKey() {
        return platformKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(platformKey, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        PlatformTimelineKey other = (PlatformTimelineKey) obj;
        return Objects.equals(this.platformKey, other.platformKey)
                && Objects.equals(this.timestamp, other.timestamp);
    }
}
