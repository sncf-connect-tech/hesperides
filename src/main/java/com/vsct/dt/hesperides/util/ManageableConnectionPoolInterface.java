package com.vsct.dt.hesperides.util;

import redis.clients.util.Pool;

/**
 * Created by emeric_martineau on 20/01/2016.
 */
public interface ManageableConnectionPoolInterface<T> {
    /**
     * Return pool of connection.
     *
     * @return pool
     */
    Pool<T> getPool();
}
