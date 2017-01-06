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

package com.vsct.dt.hesperides.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This represents an event to be sent to the view.
 * The data field is an Object, to make things easy for the view.
 *
 * Created by tidiane_sidibe on 02/03/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class EventData {

    private String type;
    private Object data;
    private long timestamp;
    private String user;

    public EventData(){

    }

    public EventData (@JsonProperty("type") final String type,
                      @JsonProperty("data") final Object data,
                      @JsonProperty("timestamp")final long timestamp,
                      @JsonProperty("user") final String user){
        this.type = type;
        this.data = data;
        this.timestamp = timestamp;
        this.user = user;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("data")
    public Object getData() {
        return data;
    }

    @JsonProperty("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @JsonProperty("user")
    public String getUser() {
        return user;
    }
}
