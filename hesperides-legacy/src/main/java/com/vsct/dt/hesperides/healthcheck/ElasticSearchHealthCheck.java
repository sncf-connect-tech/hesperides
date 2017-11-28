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

package com.vsct.dt.hesperides.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.vsct.dt.hesperides.indexation.ElasticSearchClient;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;

public final class ElasticSearchHealthCheck extends HealthCheck {
    private final HttpClient httpClient;
    private final HttpHost httpHost;

    public ElasticSearchHealthCheck(final ElasticSearchClient elasticSearchClient) {
        this.httpClient = elasticSearchClient.getClient();
        this.httpHost = elasticSearchClient.getHost();
    }

    @Override
    protected Result check() throws IOException {
        HttpGet healthRequest = null;
        try {
            healthRequest = new HttpGet("/_cluster/health");
            HttpResponse response = httpClient.execute(httpHost, healthRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                // TODO Check server's response : green, yellow, red...
                return Result.unhealthy("Hesperides can't access ElasticSearch " + this.httpHost.getHostName() + ":" + this.httpHost.getPort() + ", status code is " + statusCode + ", " + response.getStatusLine().getReasonPhrase());
            }
            return Result.healthy();
        } finally {
            if (healthRequest != null) {
                healthRequest.releaseConnection();
            }
        }
    }
}
