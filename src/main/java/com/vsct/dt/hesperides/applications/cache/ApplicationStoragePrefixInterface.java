package com.vsct.dt.hesperides.applications.cache;

import com.vsct.dt.hesperides.storage.StoragePrefixInterface;

/**
 * Created by emeric_martineau on 19/01/2016.
 */
public interface ApplicationStoragePrefixInterface extends StoragePrefixInterface {
    @Override
    default String getStreamPrefix() {
        return "platform";
    }
}
