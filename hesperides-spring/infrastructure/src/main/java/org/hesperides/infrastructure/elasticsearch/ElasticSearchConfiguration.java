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
package org.hesperides.infrastructure.elasticsearch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfiguration {
    @Value("${els.host}")
    private String host;

    @Value(("${els.index}"))
    private String index;

    @Value((("${els.port}")))
    private int port;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getIndex() {
        return index;
    }
//
//    @JsonProperty
//    private boolean reindexOnStartup;
//
//    @NotNull
//    @JsonProperty
//    private int retry;
//
//    @NotNull
//    @JsonProperty
//    private int waitBeforeRetryMs;
//

//
//    public void setHost(String host) {
//        this.host = host;
//    }
//

//
//    public void setIndex(String index) {
//        this.index = index;
//    }
//
//
//
//    public void setPort(int port) {
//        this.port = port;
//    }
//
//    public boolean reindexOnStartup() {
//        return reindexOnStartup;
//    }
//
//    public void setReindexOnStartup(boolean reindexOnStartup) {
//        this.reindexOnStartup = reindexOnStartup;
//    }
//
//    public int getRetry() {
//        return retry;
//    }
//
//    public void setRetry(int retry) {
//        this.retry = retry;
//    }
//
//    public int getWaitBeforeRetryMs() {
//        return waitBeforeRetryMs;
//    }
//
//    public void setWaitBeforeRetryMs(int waitBeforeRetryMs) {
//        this.waitBeforeRetryMs = waitBeforeRetryMs;
//    }
}
