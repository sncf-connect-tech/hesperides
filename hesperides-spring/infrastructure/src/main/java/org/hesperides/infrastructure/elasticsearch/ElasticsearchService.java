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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.hesperides.infrastructure.elasticsearch.response.ResponseHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Service
public class ElasticsearchService {
    @Autowired
    ElasticsearchClient elasticsearchClient;
    @Autowired
    ElasticsearchConfiguration elasticsearchConfiguration;


    public ResponseHits getResponseHits(final String method, final String url, final String requestBody, final TypeReference typeReference) {
        ResponseHits responseHits = null;
        RestClient restClient = this.elasticsearchClient.getRestClient();
        String endpoint = "/" + this.elasticsearchConfiguration.getIndex() + url;
        try {
            HttpEntity entity = new NStringEntity(requestBody, ContentType.APPLICATION_JSON);
            Response response = restClient.performRequest(method, endpoint, Collections.emptyMap(), entity);
            responseHits = new ObjectMapper().readValue(response.getEntity().getContent(), typeReference);
        } catch (ResponseException e) {

            // si on est sur du 404, alors tente de créer l'index.
            if (hesperidesIndexIsNotPresent(e)) {
                log.warn("Hesperides index was not found, create it");
                // on crée l'index et on recommence la requete.
                createHesperidesIndex();
                return getResponseHits(method, url, requestBody, typeReference);
            }
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return responseHits;
    }

    private boolean hesperidesIndexIsNotPresent(ResponseException e) {
        // si on est sur du 404, alors tente de créer l'index.
        return e.getResponse().getStatusLine().getStatusCode() == 404 && entityContainsIndexMissingException(e.getResponse().getEntity());
    }

    private boolean entityContainsIndexMissingException(HttpEntity entity) {
        try {
            String e = EntityUtils.toString(entity);
            return e != null && e.contains("IndexMissingException[[hesperides] missing]");
        } catch (IOException e) {
            return false;
        }
    }

    private void createHesperidesIndex() {
        try {
            elasticsearchClient.getRestClient().performRequest("PUT", "/" + this.elasticsearchConfiguration.getIndex());
        } catch (IOException e) {
            throw new RuntimeException("could not create hesperides index: " + e.getMessage(), e);
        }
    }
}
