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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.hesperides.infrastructure.elasticsearch.response.Hit;
import org.hesperides.infrastructure.elasticsearch.response.ResponseHits;
import org.hesperides.infrastructure.mustache.MustacheTemplateGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableMap.of;

@Slf4j
@Service
@Profile("!local")
public class ElasticsearchService {

    private static final MustacheFactory mustacheFactory = new DefaultMustacheFactory();
    private static final String[] MAPPINGS = new String[]{
            "evaluatedproperties",
            "templates",
            "platforms",
            "modules",
            "instances"
    };
    private static LoadingCache<String, MustacheTemplateGenerator.TemplateMaker> mustacheCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, MustacheTemplateGenerator.TemplateMaker>() {
                @Override
                public MustacheTemplateGenerator.TemplateMaker load(String key) throws Exception {
                    return MustacheTemplateGenerator.from(mustacheFactory.compile("els/queries/" + key));
                }
            });

    private final ElasticsearchConfiguration elasticsearchConfiguration;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public ElasticsearchService(ElasticsearchConfiguration elasticsearchConfiguration,
                                ObjectMapper objectMapper) {
        this.elasticsearchConfiguration = elasticsearchConfiguration;
        this.objectMapper = objectMapper;
        HttpHost httpHost = new HttpHost(this.elasticsearchConfiguration.getHost(), this.elasticsearchConfiguration.getPort());
        this.restClient = RestClient.builder(httpHost).build(); //TODO .setFailureListener()
    }

    @PostConstruct
    public void ensureMainIndexExist() throws IOException {

        if (elasticsearchConfiguration.isShouldResetIndexOnStartUp()) {
            reset();
        } else {
            String endpoint = "/" + this.elasticsearchConfiguration.getIndex();
            try {
                restClient.performRequest("GET", endpoint);
            } catch (ResponseException e) {
                if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                    log.info("hummm... looks like we need to create the hesperides index...");
                    reset();
                    log.info("done.");
                }
            }
        }
    }

    private ResponseHits getResponseHits(final String url, final String mustacheTemplate, Map<String, Object> parameters) {
        String requestBody = mustacheCache.getUnchecked(mustacheTemplate).put(parameters).generate();
        return getResponseHits("GET", url, requestBody);
    }

    private ResponseHits getResponseHits(final String method, final String url, final String requestBody) {
        String endpoint = "/" + this.elasticsearchConfiguration.getIndex() + url;
        try {
            HttpEntity entity = new NStringEntity(requestBody, ContentType.APPLICATION_JSON);
            Response response = restClient.performRequest(method, endpoint, Collections.emptyMap(), entity);

            return objectMapper.readValue(response.getEntity().getContent(), ResponseHits.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseHits search(final String document, final String mustacheTemplate, Map<String, Object> parameters) {
        return getResponseHits("/" + document + "/_search", mustacheTemplate, parameters);
    }

    public ResponseHits search(final String document, final String mustacheTemplate) {
        return getResponseHits("/" + document + "/_search", mustacheTemplate, of());
    }

    public <T> List<T> searchForSome(final String document, final String mustacheTemplate, Map<String, Object> parameters, Class<T> responseType) {
        ResponseHits search = search(document, mustacheTemplate, parameters);
        return search.getHits().getHits().stream().map(Hit::getSource).map(jsonNode -> treeToValue(responseType, jsonNode)).collect(Collectors.toList());
    }

    private <T> T treeToValue(Class<T> responseType, JsonNode jsonNode) {
        try {
            return objectMapper.treeToValue(jsonNode, responseType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> searchForSome(final String document, final String mustacheTemplate, Class<T> responseType) {
        return searchForSome(document, mustacheTemplate, of(), responseType);
    }

    public Optional<Hit> get(final String document) {
        String endpoint = "/" + this.elasticsearchConfiguration.getIndex() + "/" + document;
        try {
            Response response = restClient.performRequest("GET", endpoint);
            return Optional.of(objectMapper.readValue(response.getEntity().getContent(), Hit.class));
        } catch (ResponseException e) {
            Response response = e.getResponse();
            if (response.getStatusLine().getStatusCode() == 404) {
                return Optional.empty();
            } else {
                throw new RuntimeException("status not expected from ELS: " + response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<T> getOne(final String document, Class<T> reponseType) {
        return get(document).map(hit -> treeToValue(reponseType, hit.getSource()));
    }

    public <T> Optional<T> searchForOne(final String document, final String mustacheTemplate, Map<String, Object> parameters, Class<T> responseType) {
        ResponseHits search = search(document, mustacheTemplate, parameters);
        if (search.getHits().getHits().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(treeToValue(responseType, search.getHits().getHits().get(0).getSource()));
    }

    public Response index(String document, Object content) {
        String endpoint = "/" + this.elasticsearchConfiguration.getIndex() + "/" + document;

        try {
            HttpEntity entity = new NStringEntity(objectMapper.writeValueAsString(content), ContentType.APPLICATION_JSON);
            return restClient.performRequest("POST", endpoint, Collections.emptyMap(), entity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() throws IOException {
        /* Reset the index */
        try {
            restClient.performRequest("DELETE", "/" + this.elasticsearchConfiguration.getIndex());
        } catch (final Exception e) {
            log.info("Could not delete elastic search index. This mostly happens when there is no index already");
        }

        log.debug("Deleted Hesperides index {}", elasticsearchConfiguration.getIndex());

        /* Add global mapping */
        try (InputStream globalMappingFile = new ClassPathResource("/els/index/global_mapping.json").getInputStream()) {

            Response response = restClient.performRequest("PUT", "/" + elasticsearchConfiguration.getIndex(),
                    of(), new InputStreamEntity(globalMappingFile));

            log.debug("Put new global mapping in {}: {}", elasticsearchConfiguration.getIndex(), response.getStatusLine());
        }

        /* Add documents mapping
         */
        for (final String mapping : MAPPINGS) {

            try (InputStream mappingFile = new ClassPathResource("/els/index/" + mapping + "_mapping.json").getInputStream()) {

                Response response = restClient.performRequest("PUT",
                        "/" + elasticsearchConfiguration.getIndex() + "/" + mapping + "/_mapping",
                        of(),
                        new InputStreamEntity(mappingFile)
                );

                log.debug("Put new mapping in {}: {}", mapping, response.getStatusLine());
            }
        }

    }
}
