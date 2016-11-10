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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.vsct.dt.hesperides.templating.models.KeyValuePropertyModel;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by william_montaz on 10/07/14.
 *
 * WARNING : don't override equals for REST input object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"name", "value"})
@JsonDeserialize//This annotation is important, it prevents KeyValueValorisation from using the Valorisation Deserializer, which would cause endless loops
public final class KeyValueValorisation extends Valorisation {

    public static final Pattern valorisation_templating_pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
    private final String value;

    @JsonCreator
    public KeyValueValorisation(@JsonProperty("name") final String name,
                                @JsonProperty("value") final String value) {
        super(name);
        Preconditions.checkNotNull(value, "The value of a valorisation should not be null");
        this.value = value;
    }

    public static Set<KeyValuePropertyModel> extractWantedValorisations(String value) {
        Set<KeyValuePropertyModel> keys = Sets.newHashSet();
        Matcher matcher = valorisation_templating_pattern.matcher(value);
        while (matcher.find()) {
            KeyValuePropertyModel kvp = new KeyValuePropertyModel(matcher.group(1), "");
            keys.add(kvp);
        }
        return keys;
    }

    public String getValue() {
        //This helps to deal with old values that have been set to null
        return (value == null) ? "" : value;
    }

    @Override
    public Valorisation inject(Map<String, String> injectedValues) {
        return new KeyValueValorisation(this.getName(), injectMapOfKeyValueInTemplateString(this.getValue(), injectedValues));
    }

    private String injectMapOfKeyValueInTemplateString(String value, final Map<String, String> context) {
        return replaceWithPattern(value, valorisation_templating_pattern, captured -> {

            // TODO : this should be trimed as in KeyValueValorisationData ?
            String capture = captured.group(1);

            String replacement = context.get(capture);
            if (replacement == null) {
                return captured.group();
            }
            else {
                return replacement;
            }
        });
    }

    private String replaceWithPattern(final String s, final Pattern p, final Function<Matcher, String> replacement) {
        Matcher matcher = p.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, replacement.apply(matcher));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    public String toString() {
        return "key/value: " + this.getName() + "=" + value;
    }

}
