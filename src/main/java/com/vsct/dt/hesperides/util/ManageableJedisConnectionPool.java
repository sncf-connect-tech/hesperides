/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides.util;

import com.vsct.dt.hesperides.storage.RedisConfiguration;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

/**
 * Created by william_montaz on 23/04/2015.
 */
public class ManageableJedisConnectionPool implements Managed {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManageableJedisConnectionPool.class);

    private final Pool<Jedis> pool;

    public ManageableJedisConnectionPool(Pool<Jedis> pool) {
        this.pool = pool;
    }

    public Pool<Jedis> getPool() {
        return pool;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {
        LOGGER.info("DESTROYING REDIS CONNECTION POOL");
        pool.destroy();
    }

    public static ManageableJedisConnectionPool createPool(RedisConfiguration redisConfiguration) {
        switch (redisConfiguration.getType()) {
            case REDIS: {
                LOGGER.info("Creates Simple Redis connection pool");
                return new ManageableJedisConnectionPool(
                        new JedisPool(
                                new JedisPoolConfig(), redisConfiguration.getHost(), redisConfiguration.getPort(),
                                redisConfiguration.getTimeout()
                                ));
            }
            case SENTINEL: {
                LOGGER.info("Creates Redis Sentinel connection pool");
                return new ManageableJedisConnectionPool(
                        new JedisSentinelPool(
                                redisConfiguration.getMasterName(), redisConfiguration.getSentinels(),
                                new JedisPoolConfig(), redisConfiguration.getTimeout()));
            }
            default: {
                throw new IllegalArgumentException("Unexpected jedis pool type provided in configuration file");
            }
        }

    }


}
