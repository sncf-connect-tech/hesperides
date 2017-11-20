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

package com.vsct.dt.hesperides.indexation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vsct.dt.hesperides.exception.runtime.NonUniqueResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Deserialization des réponses venant d'ElasticSearch.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticSearchResponse<T> extends ElasticSearchError {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchResponse.class);
    private long took;
    private boolean timedOut;
    private Map<String, Object> shards;
    private ElasticSearchHitsResponse<T> hits;

    public ElasticSearchResponse() {
    }

    @JsonProperty(value = "took")
    public long getTook() {
        return took;
    }

    public void setTook(final long took) {
        this.took = took;
    }

    @JsonProperty(value = "timed_out")
    public boolean isTimedOut() {
        return timedOut;
    }

    public void setTimedOut(final boolean timedOut) {
        this.timedOut = timedOut;
    }

    @JsonProperty(value = "_shards")
    public Map<String, Object> getShards() {
        return shards;
    }

    public void setShards(final Map<String, Object> shards) {
        this.shards = shards;
    }

    @JsonProperty(value = "hits")
    public ElasticSearchHitsResponse<T> getHits() {
        return hits;
    }

    public void setHits(final ElasticSearchHitsResponse<T> hits) {
        this.hits = hits;
    }

    public Stream<T> streamOfData() {
        LOGGER.debug("get {} hits from elasticsearch", this.getHits().getTotal());
        return this.getHits().getDocuments().stream()
                .filter(ElasticSearchDocument<T>::hasData)
                .map(ElasticSearchDocument<T>::getData);
    }

    public Optional<T> getSingleResult() throws NonUniqueResultException {
        if (this.getHits().getTotal() > 1) {
            LOGGER.error("Get {} hits but we're expecting only one", this.getHits());

            //throw new ESServiceException("Failed to get instance with id " + id, url, body, httpHost.getHostName(), httpHost.getPort(), index);
            //TO DO Handle new exception type, it is not due to ES
            //Encapsulate in elastic search response
            throw new NonUniqueResultException("Found several instances of " + this.getHits().getDocuments().get(0).getData().getClass() + " but wanted single result");
        }
        if (this.getHits().getTotal() == 1) {
            LOGGER.debug("Data is null ?", this.getHits().getDocuments().get(0).getData());
            return Optional.of(this.getHits().getDocuments().get(0).getData());
        } else {
            LOGGER.debug("No result from ELS");
            return Optional.empty();
        }
    }

    public Stream<ElasticSearchDocument<T>> streamOfDocument() {
        return this.getHits().getDocuments().stream();
    }

    public long getHitsNumber() {
        if (this.getHits() != null) {
            return this.getHits().getTotal();
        } else return 0;
    }
}

