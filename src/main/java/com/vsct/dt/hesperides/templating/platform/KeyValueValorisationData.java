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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.vsct.dt.hesperides.applications.MustacheScopeEntry;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by william_montaz on 10/07/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"name", "value"})
@JsonDeserialize//This annotation is important, it prevents KeyValueValorisation from using the Valorisation Deserializer, which would cause endless loops
public final class KeyValueValorisationData extends ValorisationData {

    public static final Pattern VALORISATION_TEMPLATING_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");
    private String value;

    @JsonCreator
    public KeyValueValorisationData(@JsonProperty("name") final String name,
                                    @JsonProperty("value") final String value) {
        super(name);
        Preconditions.checkNotNull(value, "The value of a valorisation should not be null");
        this.value = value;
    }

    public String getValue() {
        //This helps to deal with old values that have been set to null
        if (value == null) return "";
        else return value;
    }

    @Override
    public MustacheScopeEntry<String, Object> toMustacheScopeEntry() {
        return new MustacheScopeEntry<>(this.getName(), this.getValue());
    }

    @Override
    public ValorisationData inject(Map<String, String> injectedValues) {
        return new KeyValueValorisationData(this.getName(), injectMapOfKeyValueInTemplateString(this.getValue(), injectedValues));
    }

    private String injectMapOfKeyValueInTemplateString(String value, final Map<String, String> context) {
        return replaceWithPattern(value, VALORISATION_TEMPLATING_PATTERN, captured -> {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        KeyValueValorisationData that = (KeyValueValorisationData) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "key/value: " + this.getName() + "=" + value;
    }

}
