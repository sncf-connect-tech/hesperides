package com.vsct.dt.hesperides.templating.packages.cache;

import com.vsct.dt.hesperides.storage.StoragePrefixInterface;

/**
 * Created by emeric_martineau on 19/01/2016.
 */
public interface TemplatePackageStoragePrefixInterface extends StoragePrefixInterface {
    @Override
    default String getStreamPrefix() {
        return "template_package";
    }
}
