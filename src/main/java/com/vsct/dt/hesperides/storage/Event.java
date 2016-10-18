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

package com.vsct.dt.hesperides.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.vsct.dt.hesperides.security.model.User;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Objects;

/**
* Created by william_montaz on 22/01/2015.
*/
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"eventType", "data"})
public final class Event {

    private final String eventType;
    private final String data;
    private final long timestamp;
    private final String user;

    @JsonCreator
    public Event(@JsonProperty("eventType") final String eventType,
                 @JsonProperty("data") final String data,
                 @JsonProperty("timestamp") final long timestamp,
                 @JsonProperty("user") final String user) {
        this.eventType = eventType;
        this.data = data;
        this.timestamp = timestamp;
        this.user = user;
    }

    @JsonProperty(value = "eventType")
    public String getEventType() {
        return eventType;
    }

    @JsonProperty(value = "data")
    public String getData() {
        return data;
    }

    @JsonProperty(value = "timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @JsonProperty(value = "user")
    public String getUser() {
        return user;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, data, timestamp, user);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Event other = (Event) obj;
        return Objects.equals(this.eventType, other.eventType)
                && Objects.equals(this.data, other.data)
                && Objects.equals(this.timestamp, other.timestamp)
                && Objects.equals(this.user, other.user);
    }
}
