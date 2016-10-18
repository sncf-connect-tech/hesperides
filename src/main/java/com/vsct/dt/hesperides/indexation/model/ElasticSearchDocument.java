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

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ElasticSearchDocument<T> {

    // Elastic search information for a document
    private String type;
    private long score;
    private String index;
    private String id;
    private Map<String, List<String>> fields;

    // Source data with business values
    private T data;

    public ElasticSearchDocument() {
        //Jackson
    }

    @JsonProperty(value = "_id")
    public String getId() {
        return id;
    }

    @JsonProperty(value = "_type")
    public String getType() {
        return type;
    }

    @JsonProperty(value = "_score")
    public long getScore() {
        return score;
    }

    @JsonProperty(value = "_index")
    public String getIndex() {
        return index;
    }

    @JsonProperty
    public Map<String, List<String>> getFields() {
        return fields;
    }

    public void setFields(final Map<String, List<String>> fields) {
        this.fields = fields;
    }

    public String getStringField(final String field) {
        checkNotNull(this.fields);
        checkNotNull(this.fields.get(field));
        checkState(this.fields.get(field).size() == 1);
        return this.fields.get(field).get(0);
    }

    @JsonProperty(value = "_source")
    public T getData() {
        checkNotNull(data, "the data " + id + " has no data (document).");
        return data;
    }

    public boolean hasData() {
        return data != null;
    }


    @Override
    public String toString() {
        return "ElasticSearchDocument{" +
                "type='" + type + '\'' +
                ", score=" + score +
                ", index='" + index + '\'' +
                ", id='" + id + '\'' +
                ", fields=" + fields +
                ", data=" + data +
                '}';
    }

}
