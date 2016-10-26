package com.vsct.dt.hesperides.storage;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by william_montaz on 29/01/2015.
 */
public class RetryRedisConfiguration extends RedisConfiguration {
    /* All */
    @JsonProperty
    private int retry;

    @JsonProperty
    private int waitBeforeRetryMs;

    @Override
    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    @Override
    public int getWaitBeforeRetryMs() {
        return waitBeforeRetryMs;
    }

    public void setWaitBeforeRetryMs(int waitBeforeRetryMs) {
        this.waitBeforeRetryMs = waitBeforeRetryMs;
    }
}
