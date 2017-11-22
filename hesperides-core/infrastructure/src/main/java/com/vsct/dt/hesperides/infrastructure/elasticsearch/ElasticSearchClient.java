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

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import javax.inject.Inject;

public class ElasticSearchClient {

    private final ElasticSearchConfiguration elasticSearchConfiguration;

    @Inject
    public ElasticSearchClient(final ElasticSearchConfiguration elasticSearchConfiguration) {
        this.elasticSearchConfiguration = elasticSearchConfiguration;
    }

    public RestClient getRestClient() {
        HttpHost httpHost = new HttpHost(this.elasticSearchConfiguration.getHost(), this.elasticSearchConfiguration.getPort());
        return RestClient.builder(httpHost).build(); //TODO .setFailureListener()
    }
}
