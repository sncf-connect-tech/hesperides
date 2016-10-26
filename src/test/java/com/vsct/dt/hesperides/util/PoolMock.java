package com.vsct.dt.hesperides.util;

import redis.clients.util.Pool;

/**
 * Created by emeric_martineau on 02/06/2016.
 */
public class PoolMock extends Pool<JedisMock> {
    /**
     * Fake redis connection.
     */
    private final JedisMock jedisMock = new JedisMock();

    @Override
    public JedisMock getResource() {
        return this.jedisMock;
    }

    public void clear() {
        this.jedisMock.clear();
    }
}
