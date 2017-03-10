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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 10/07/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"key_value_properties", "iterable_properties"})
public final class Properties {

    @JsonProperty("key_value_properties")
    private final Set<KeyValueValorisation> keyValueProperties;

    @JsonProperty("iterable_properties")
    private final Set<IterableValorisation> iterableProperties;

    @JsonCreator
    public Properties(@JsonProperty("key_value_properties") final Set<KeyValueValorisation> keyValueProperties,
            @JsonProperty("iterable_properties") final Set<IterableValorisation> iterableProperties) {
        this.keyValueProperties = Sets.newHashSet(keyValueProperties);
        this.iterableProperties = Sets.newHashSet(iterableProperties);
    }

    public static Properties empty() {
        return new Properties(Sets.newHashSet(), Sets.newHashSet());
    }

    /**
     * Copies the valuations without empty ones
     *
     * @param valorisations : the valuations to be copied
     * @return : the copy of the valuations
     */
    private Set<Valorisation> copyValorisations ( Set<Valorisation> valorisations){

        Set<Valorisation> copy = Sets.newHashSet();
        for ( Valorisation val : valorisations ){
            if ( val instanceof KeyValueValorisation ){
                KeyValueValorisation _val = (KeyValueValorisation) val;

                if ( !Strings.isNullOrEmpty(_val.getValue())){
                    copy.add(_val);
                }
            }else{
                copy.add(copyIterableValorisation((IterableValorisation)val));
            }
        }

        return copy;
    }

    /**
     * Copy iterable items without empty values
     *
     * @param item : the item to be copied
     *
     * @return the copy of the item
     */
    private IterableValorisation.IterableValorisationItem copyItem (IterableValorisation.IterableValorisationItem item){
        Set<Valorisation> values = copyValorisations(item.getValues());
        IterableValorisation.IterableValorisationItem copy = new IterableValorisation.IterableValorisationItem(item.getTitle(), values);
        return copy;
    }

    /**
     *  Copy iterable valuations without empty values
     *
     * @param iterable : the iterable to be copied
     *
     * @return the copy of the iterable
     */
    private IterableValorisation copyIterableValorisation (IterableValorisation iterable){
        List<IterableValorisation.IterableValorisationItem> items = Lists.newArrayList();

        for (IterableValorisation.IterableValorisationItem item : iterable.getIterableValorisationItems() ){
            items.add(copyItem(item));
        }

        IterableValorisation copy = new IterableValorisation(iterable.getName(), items);
        return copy;
    }

    /**
     * Copy all properties without empty values
     *
     * @return the copy of the properties
     */
    public Properties makeCopyWithoutNullOrEmptyValorisations() {

        // For key value properties
        Set<KeyValueValorisation> keyValuePropertiesCleaned = this.keyValueProperties
                .stream()
                .filter(kvp -> kvp.getValue() != null && !kvp.getValue().isEmpty())
                .collect(Collectors.toSet());

        // For iterable properties
        Set<IterableValorisation> iterablePropertiesCleaned = Sets.newHashSet();

        for (Valorisation val : this.iterableProperties){
            iterablePropertiesCleaned.add(copyIterableValorisation((IterableValorisation) val));
        }

        // cleaned
        return new Properties(keyValuePropertiesCleaned, iterablePropertiesCleaned);
    }

    public Set<KeyValueValorisation> getKeyValueProperties() {
        return Sets.newHashSet(keyValueProperties);
    }

    public Set<IterableValorisation> getIterableProperties() {
        return Sets.newHashSet(iterableProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyValueProperties, iterableProperties);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Properties other = (Properties) obj;
        return Objects.equals(this.keyValueProperties, other.keyValueProperties)
                && Objects.equals(this.iterableProperties, other.iterableProperties);
    }
}