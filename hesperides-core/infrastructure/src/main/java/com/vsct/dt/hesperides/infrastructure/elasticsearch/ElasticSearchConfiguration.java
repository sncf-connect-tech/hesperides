/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package com.vsct.dt.hesperides.infrastructure.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class ElasticSearchConfiguration {
    @JsonProperty
    private String host;

    @JsonProperty
    private String index;

    @NotNull
    @JsonProperty
    private int port;

    @JsonProperty
    private boolean reindexOnStartup;

    @NotNull
    @JsonProperty
    private int retry;

    @NotNull
    @JsonProperty
    private int waitBeforeRetryMs;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean reindexOnStartup() {
        return reindexOnStartup;
    }

    public void setReindexOnStartup(boolean reindexOnStartup) {
        this.reindexOnStartup = reindexOnStartup;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public int getWaitBeforeRetryMs() {
        return waitBeforeRetryMs;
    }

    public void setWaitBeforeRetryMs(int waitBeforeRetryMs) {
        this.waitBeforeRetryMs = waitBeforeRetryMs;
    }
}
