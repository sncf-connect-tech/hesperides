package com.vsct.dt.hesperides.storage;


import redis.clients.jedis.*;

import java.io.Closeable;

/**
 * Created by emeric_martineau on 30/05/2016.
 */
public interface RedisCommand<T, A extends JedisCommands&MultiKeyCommands&AdvancedJedisCommands&ScriptingCommands&BasicCommands&ClusterCommands&Closeable> {
    T execute(A jedis) throws Throwable;
    void error(final Throwable e);
}
