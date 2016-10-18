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
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Optional;

/**
 * Created by william_montaz on 16/07/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ElasticSearchEntity<T> extends ElasticSearchError {

    private String index;
    private String type;
    private String id;
    private String version;
    private boolean found;
    private boolean created;
    private Get<T> get;

    @JsonProperty(value = "_index")
    public String getIndex() {
        return index;
    }

    public void setIndex(final String index) {
        this.index = index;
    }

    @JsonProperty(value = "_type")
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @JsonProperty(value = "_id")
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @JsonProperty(value = "_version")
    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    @JsonProperty(value = "get")
    public Get<T> getGet() {
        return get;
    }

    public void setGet(final Get<T> get) {
        this.get = get;
    }

    @JsonProperty(value = "created")
    public boolean isCreated() {
        return created;
    }

    public void setCreated(final boolean created) {
        this.created = created;
    }

    public Optional<T> getEntity() {
        if (this.get != null && this.get.isFound()) {
            return Optional.of(this.get.getSource());
        } else {
            return Optional.empty();
        }
    }

    @JsonSnakeCase
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Get<T> {
        private boolean found;
        private T source;

        @JsonProperty(value = "_source")
        public T getSource() {
            return source;
        }

        public void setSource(final T source) {
            this.source = source;
        }

        public boolean isFound() {
            return found;
        }

        public void setFound(final boolean found) {
            this.found = found;
        }
    }
}
