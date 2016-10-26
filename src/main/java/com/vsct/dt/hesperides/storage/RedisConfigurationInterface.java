package com.vsct.dt.hesperides.storage;

import java.util.Set;

/**
 * Created by emeric_martineau on 29/04/2016.
 */
public interface RedisConfigurationInterface {
    String getHost();

    int getPort();

    String getMasterName();

    Set<String> getSentinels();

    RedisConfiguration.Type getType();

    int getRetry();

    int getWaitBeforeRetryMs();

    int getTimeout();
}
