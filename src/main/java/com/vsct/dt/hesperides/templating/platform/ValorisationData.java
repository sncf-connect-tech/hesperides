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

package com.vsct.dt.hesperides.templating.platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.vsct.dt.hesperides.applications.MustacheScopeEntry;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by william_montaz on 28/07/2015.
 *
 * Modified by tidiane_sidibe on 10/11/2016 : Adding whitespaces ignoring when using global properties
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = ValorisationData.JacksonDeserializer.class)
/* Cannot use abstract class because of jackson throwing a "cannot find JsonCreator for property exception*/
public class ValorisationData {

    private String name;

    @JsonCreator
    public ValorisationData(@JsonProperty("name") final String name) {
        checkNotNull(!Strings.isNullOrEmpty(name), "A valorisation name should not be empty or null");

        // At this point, name should not be null, but it's seems to be tha case for some stored data !
        // So we trim only if not null !
        this.name = (name != null) ? name.trim() : name;
    }

    @JsonProperty("name")
    public String getName() {
        return name.trim();
    }

    public MustacheScopeEntry<String, Object> toMustacheScopeEntry(){
        throw new NotImplementedException("");
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValorisationData that = (ValorisationData) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public ValorisationData inject(Map<String, String> keyValueProperties){
        throw new NotImplementedException("");
    };

    /* Use a specific deserializer to handle polymorphism and avoid changing the existing API */
    public static class JacksonDeserializer extends StdDeserializer<ValorisationData> {

        protected JacksonDeserializer() {
            super(ValorisationData.class);
        }

        @Override
        public ValorisationData deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            ObjectNode obj = mapper.readTree(jp);

            if(obj.get("value") != null){
                /* This is a KeyValueValorisation */
                return mapper.treeToValue(obj, KeyValueValorisationData.class);
            } else {
                /* No other choice, it is an iterable valorisation */
                return mapper.treeToValue(obj, IterableValorisationData.class);
            }

        }
    }

}
