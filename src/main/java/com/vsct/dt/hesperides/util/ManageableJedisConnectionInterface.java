package com.vsct.dt.hesperides.util;

/**
 * Created by emeric_martineau on 29/04/2016.
 */
public interface ManageableJedisConnectionInterface<T> extends ManageableRedisConnectionInterface,
    ManageableConnectionPoolInterface<T> {
}
