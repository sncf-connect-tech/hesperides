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
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.vsct.dt.hesperides.applications.InstanceModel;
import com.vsct.dt.hesperides.applications.MustacheScope;
import com.vsct.dt.hesperides.applications.MustacheScope.InjectableMustacheScope;
import com.vsct.dt.hesperides.templating.models.KeyValuePropertyModel;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 10/07/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"key_value_properties", "iterable_properties"})
public final class PropertiesData {

    private final Set<KeyValueValorisationData> keyValueProperties;
    private final Set<IterableValorisationData> iterableProperties;

    @JsonCreator
    public PropertiesData(@JsonProperty("key_value_properties") final Set<KeyValueValorisationData> keyValueProperties,
                          @JsonProperty("iterable_properties") final Set<IterableValorisationData> iterableProperties) {
        Preconditions.checkNotNull(keyValueProperties, "key_value_properties should not be null (at least an empty set)");
        Preconditions.checkNotNull(iterableProperties, "iterable_properties should not be null (at least an empty set)");
        this.keyValueProperties = Sets.newHashSet(keyValueProperties);
        this.iterableProperties = Sets.newHashSet(iterableProperties);
    }

    public static PropertiesData empty() {
        return new PropertiesData(Sets.newHashSet(), Sets.newHashSet());
    }

    public MustacheScope toMustacheScope(Set<KeyValueValorisationData> instanceValorisations, Set<KeyValueValorisationData> platformValorisations) {
        return toMustacheScope(instanceValorisations, platformValorisations, false);
    }

    public MustacheScope toMustacheScope(Set<KeyValueValorisationData> instanceValorisations, Set<KeyValueValorisationData> platformValorisations, Boolean buildingFile) {
        if (instanceValorisations == null) {
            instanceValorisations = new HashSet<>();
        }
        if (platformValorisations == null) {
            platformValorisations = new HashSet<>();
        }

        HashSet<ValorisationData> valorisations = new HashSet<>();
        valorisations.addAll(keyValueProperties);
        valorisations.addAll(iterableProperties);
        if (platformValorisations != null) {
            /* addAll doesn't replace existing values, but we want to, so iterate */
            for (KeyValueValorisationData v : platformValorisations) {
                //Remove local valorisation if it exists (ie has the same name even if the value is different)
                for (Iterator<ValorisationData> it = valorisations.iterator(); it.hasNext(); ) {
                    ValorisationData existingValorisation = it.next();
                    if (existingValorisation.getName().equals(v.getName())) {
                        it.remove();
                    }
                }
                valorisations.add(v);
            }
        }

        /* Prepare what will be injected in the values */
        Map<String, String> injectableKeyValueValorisations = keyValueProperties.stream().collect(Collectors.toMap(ValorisationData::getName, KeyValueValorisationData::getValue));
        Map<String, String> injectablePlatformValorisations = platformValorisations.stream().collect(Collectors.toMap(ValorisationData::getName, KeyValueValorisationData::getValue));

        /* Erase local valorisations by platform valorisations */
        injectableKeyValueValorisations.putAll(injectablePlatformValorisations);

        Map<String, String> injectableInstanceProperties = instanceValorisations.stream().collect(Collectors.toMap(ValorisationData::getName, KeyValueValorisationData::getValue));

        injectableKeyValueValorisations.replaceAll((key, value) -> value.replace("$", "\\$"));
        injectableInstanceProperties.replaceAll((key, value) -> value.replace("$", "\\$"));

        InjectableMustacheScope injectable = MustacheScope.from(valorisations)
                /* First re-inject keyValueValorisations, so they can refer to themselves */
                .inject(injectableKeyValueValorisations)
                /* Do it a second time in case global properties where referring to themselves */
                .inject(injectableKeyValueValorisations)
                /* Finally inject instance valorisations */
                .inject(injectableInstanceProperties)
                /* Do it a third time in case instances properties where referring to global properties */
                .inject(injectableKeyValueValorisations);

        MustacheScope mustacheScope = injectable.create();

        if (mustacheScope.getMissingKeyValueProperties().size() > 0 && buildingFile) {

            Map<String, String> missing_valuation = new HashMap<>();

            Set<KeyValuePropertyModel> missing = mustacheScope.getMissingKeyValueProperties();

            missing.stream().forEach(prop -> {
                missing_valuation.put(prop.getName(), "");
            });

            mustacheScope = injectable.inject(missing_valuation).create();
        }

        return mustacheScope;
    }

    public InstanceModel generateInstanceModel(Set<KeyValueValorisationData> platformValorisations) {
        /* Easiest way is to generate the scope without instance valorisations, and then look in it for missing references.
           Those missing references should have been given by the instance
        */
        MustacheScope scope = this.toMustacheScope(Sets.newHashSet(), platformValorisations);

        Set<KeyValuePropertyModel> instanceModelKeyValues = scope.getMissingKeyValueProperties();
        return new InstanceModel(instanceModelKeyValues);
    }

    public Set<KeyValueValorisationData> getKeyValueProperties() {
        return Sets.newHashSet(keyValueProperties);
    }

    public Set<IterableValorisationData> getIterableProperties() {
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
        final PropertiesData other = (PropertiesData) obj;
        return Objects.equals(this.keyValueProperties, other.keyValueProperties)
                && Objects.equals(this.iterableProperties, other.iterableProperties);
    }
}
