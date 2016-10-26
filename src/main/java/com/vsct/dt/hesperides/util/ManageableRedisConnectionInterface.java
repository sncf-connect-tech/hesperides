package com.vsct.dt.hesperides.util;

import io.dropwizard.lifecycle.Managed;

/**
 * Created by emeric_martineau on 29/04/2016.
 */
public interface ManageableRedisConnectionInterface extends Managed {
    int getnRetry();

    int getWaitBeforeRetryMs();
}
