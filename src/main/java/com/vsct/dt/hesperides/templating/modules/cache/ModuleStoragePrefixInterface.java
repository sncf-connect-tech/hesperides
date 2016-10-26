package com.vsct.dt.hesperides.templating.modules.cache;

import com.vsct.dt.hesperides.storage.StoragePrefixInterface;

/**
 * Created by emeric_martineau on 19/01/2016.
 */
public interface ModuleStoragePrefixInterface extends StoragePrefixInterface {
    @Override
    default String getStreamPrefix() {
        return "module";
    }
}
