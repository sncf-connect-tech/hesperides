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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsct.dt.hesperides.infrastructure.elasticsearch.response.ResponseHits;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;

public class ElasticSearchService {
    private final ElasticSearchClient elasticSearchClient;
    private final ElasticSearchConfiguration elasticSearchConfiguration;

    @Inject
    public ElasticSearchService(final ElasticSearchClient elasticSearchClient, final ElasticSearchConfiguration elasticSearchConfiguration) {
        this.elasticSearchClient = elasticSearchClient;
        this.elasticSearchConfiguration = elasticSearchConfiguration;
    }

    public ResponseHits getResponseHits(final String method, final String url, final String requestBody, final TypeReference typeReference) {
        ResponseHits responseHits = null;
        RestClient restClient = this.elasticSearchClient.getRestClient();
        String endpoint = "/" + this.elasticSearchConfiguration.getIndex() + url;
        try {
            HttpEntity entity = new NStringEntity(requestBody, ContentType.APPLICATION_JSON);
            Response response = restClient.performRequest(method, endpoint, Collections.emptyMap(), entity);
            responseHits = new ObjectMapper().readValue(response.getEntity().getContent(), typeReference);
        } catch (IOException e) {
            e.printStackTrace(); //TODO Gérer l'exception
            throw new RuntimeException(e);
        }
        return responseHits;
    }
}
