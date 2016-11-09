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

package com.vsct.dt.hesperides.resources;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.NotImplementedException;

import java.io.IOException;
import java.util.Map;

/**
 * Created by william_montaz on 28/07/2015.
 * Updated by Tidiane SIDIBE on 09/11/2016 : Add whitespaces ignoring on properties name
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = Valorisation.JacksonDeserializer.class)
/* Cannot use abstract class because of jackson throwing a "cannot find JsonCreator for property exception*/
public class Valorisation {

    /**
     * The valuation's name.
     * Note : This is foreight key like for joining model and valuation.
     */
    private String name;

    /**
     * The
     * @param name: the name of the valuation
     */
    @JsonCreator
    protected Valorisation(@JsonProperty("name") final String name) {

        // We trim this for ignore whitespaces on valuation's name.
        // The same operation is done on property model's name.
        this.name = name.trim();
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Valorisation that = (Valorisation) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public Valorisation inject(Map<String, String> keyValueProperties){
        throw new NotImplementedException();
    }

    /* Use a specific deserializer to handle polymorphism and avoid changing the existing API */
    public static class JacksonDeserializer extends StdDeserializer<Valorisation> {

        protected JacksonDeserializer() {
            super(Valorisation.class);
        }

        @Override
        public Valorisation deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            ObjectNode obj = mapper.readTree(jp);

            if(obj.get("value") != null){
                /* This is a KeyValueValorisation */
                return mapper.treeToValue(obj, KeyValueValorisation.class);
            } else {
                /* No other choice, it is an iterable valorisation */
                return mapper.treeToValue(obj, IterableValorisation.class);
            }

        }
    }

}
