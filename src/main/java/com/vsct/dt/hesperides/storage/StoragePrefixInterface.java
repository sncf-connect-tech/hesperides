package com.vsct.dt.hesperides.storage;

/**
 * Created by emeric_martineau on 19/01/2016.
 */
public interface StoragePrefixInterface {
    /**
     * Return prefix for database.
     *
     * @return prefix string
     */
    String getStreamPrefix();
}
