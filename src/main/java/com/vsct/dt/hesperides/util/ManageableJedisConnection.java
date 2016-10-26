package com.vsct.dt.hesperides.util;

import com.vsct.dt.hesperides.storage.RedisConfigurationInterface;
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
public class ManageableJedisConnection implements
        ManageableJedisConnectionInterface {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ManageableJedisConnection.class);

    /**
     * Pool.
     */
    private Pool<Jedis> pool;

    /**
     * Number of retry.
     */
    private final int nRetry;

    /**
     * Wait time before retry.
     */
    private final int waitBeforeRetryMs;

    /**
     * Getter of pool.
     *
     * @return pool of connection.
     */
    @Override
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

    /**
     * Constructor.
     *
     * @param redisConfiguration configuration
     */
    public ManageableJedisConnection(final RedisConfigurationInterface redisConfiguration) {
        this.nRetry = redisConfiguration.getRetry();
        this.waitBeforeRetryMs = redisConfiguration.getWaitBeforeRetryMs();

        switch (redisConfiguration.getType()) {
            case REDIS:
                LOGGER.info("Creates Simple Redis connection pool");
                this.pool = new JedisPool(new JedisPoolConfig(), redisConfiguration.getHost(),
                        redisConfiguration.getPort(), redisConfiguration.getTimeout());
                break;
            case SENTINEL:
                LOGGER.info("Creates Redis Sentinel connection pool");
                this.pool = new JedisSentinelPool(redisConfiguration.getMasterName(), redisConfiguration.getSentinels(),
                        new JedisPoolConfig(), redisConfiguration.getTimeout());
                break;
            default: {
                throw new IllegalArgumentException("Unexpected jedis pool type provided in configuration file");
            }
        }

    }

    /**
     * Number of retry.
     *
     * @return number
     */
    @Override
    public int getnRetry() {
        return nRetry;
    }

    /**
     * * Wait time before retry.
     *
     * @return time in millisecond
     */
    @Override
    public int getWaitBeforeRetryMs() {
        return waitBeforeRetryMs;
    }
}
