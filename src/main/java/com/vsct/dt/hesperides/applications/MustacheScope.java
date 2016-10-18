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

package com.vsct.dt.hesperides.applications;

import com.google.common.base.Preconditions;
import com.vsct.dt.hesperides.resources.KeyValueValorisation;
import com.vsct.dt.hesperides.templating.models.KeyValuePropertyModel;
import com.vsct.dt.hesperides.templating.platform.ValorisationData;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 20/08/2015.
 */
/**
 * We use a two pass processes.
 * That allows properties to refer to themselves
 * All remaining properties are considered to be instance properties,
 * even if they have the same name as some properties
 * (that might happen if properties call themselves back and forth) like :
 * key1 -> {{key2}}
 * key2 -> {{key1}}
 * That would result in
 * key1 -> {{key1}}
 * key2 -> {{key2}}
 * Such a thing would be bad properties design but we don't want to fail
 * Generic case looks more like
 * key1 -> something {{key2}}
 * key2 -> something else
 * that would result in
 * key1 -> something something else
 * key2 -> something else
 * And further
 * key1 -> something {{key2}}
 * key2 -> {{key2}}
 * that would result in
 * key1 -> something {{key2}}
 * key2 -> {{key2}}
 * See tests
 * <p>
 * Same stuff apply to iterable valorisations. The only difference is that we only allow KeyValueValorisations to be injected in iterable valorisations
 * otherwise it gets to complicated to figure out the result from a user perspective.
 */
public class MustacheScope implements Map<String, Object> {

    public Map<String, Object>  scope = new HashMap<>();

    public MustacheScope(Set<ValorisationData> valorisations){
        for(ValorisationData valorisation: valorisations){
            MustacheScopeEntry<String, Object> entry = valorisation.toMustacheScopeEntry();
            scope.put(entry.getKey(), entry.getValue());
        }
    }

    public Set<KeyValuePropertyModel> getMissingKeyValueProperties() {
        return getMissingKeyValueProperties(scope);


    }

    private Set<KeyValuePropertyModel> getMissingKeyValueProperties(Map<String, Object> scope){
        /* Iterate through the scope. There can be string scope object or lists (depending if it comes from KeyValue or Iterable properties) */
        Set<KeyValuePropertyModel> missingKeyValueProperties = new HashSet<>();
        for (Map.Entry<String, Object> entry : scope.entrySet()) {
            Object scopeObject = entry.getValue();
            if (scopeObject instanceof String) {
                String value = (String) scopeObject;
                missingKeyValueProperties.addAll(KeyValueValorisation.extractWantedValorisations(value));
            }
            if (scopeObject instanceof List) {
                /* These are Lists of scopes */
                List<Map<String, Object>> blocksOfvalorisations = (List) scopeObject;
                blocksOfvalorisations.forEach(block -> {
                    missingKeyValueProperties.addAll(getMissingKeyValueProperties(block));
                });
            }
        }
        return missingKeyValueProperties;
    }

    /* Builder */
    public static InjectableMustacheScope from(Set<ValorisationData> valorisations) {
        return new InjectableMustacheScope(new HashSet<>(valorisations));
    }

    public static class InjectableMustacheScope {

        private final Set<ValorisationData> valorisations;
        private InjectableMustacheScope(Set<ValorisationData> valorisations) {
            Preconditions.checkNotNull(valorisations);
            this.valorisations = valorisations;
        }

        public InjectableMustacheScope inject(Map<String, String> injectedValues) {
            if(injectedValues == null || injectedValues.size() == 0) return this;
            Set<ValorisationData> injectedValorisations = valorisations.stream().map(valorisation -> valorisation.inject(injectedValues)).collect(Collectors.toSet());
            return new InjectableMustacheScope(injectedValorisations);
        }

        public MustacheScope create() {
            return new MustacheScope(valorisations);
        }
    }

    /* MAP implem, just link to the inner map */

    @Override
    public int size() {
        return scope.size();
    }

    @Override
    public boolean isEmpty() {
        return scope.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return scope.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return scope.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return scope.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return scope.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return scope.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        scope.putAll(m);
    }

    @Override
    public void clear() {
        scope.clear();
    }

    @Override
    public Set<String> keySet() {
        return scope.keySet();
    }

    @Override
    public Collection<Object> values() {
        return scope.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return scope.entrySet();
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        return scope.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        scope.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
        scope.replaceAll(function);
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        return scope.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return scope.remove(key, value);
    }

    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
        return scope.replace(key, oldValue, newValue);
    }

    @Override
    public Object replace(String key, Object value) {
        return scope.replace(key, value);
    }

    @Override
    public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
        return scope.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return scope.computeIfPresent(key, remappingFunction);
    }

    @Override
    public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return scope.compute(key, remappingFunction);
    }

    @Override
    public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return scope.merge(key, value, remappingFunction);
    }


}
