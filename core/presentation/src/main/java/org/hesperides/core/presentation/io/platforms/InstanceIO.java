/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.core.presentation.io.platforms;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.Instance;
import org.hesperides.core.domain.platforms.queries.views.InstanceView;
import org.hesperides.core.presentation.io.OnlyPrintableCharacters;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class InstanceIO {

    @OnlyPrintableCharacters(subject = "deployedModules.instances.name")
    String name;
    @NotNull
    @SerializedName("key_values")
    List<ValuedPropertyIO> valuedProperties;

    public InstanceIO(InstanceView instanceView) {
        this.name = instanceView.getName();
        this.valuedProperties = ValuedPropertyIO.fromValuedPropertyViews(instanceView.getValuedProperties());
    }

    public Instance toDomainInstance() {
        return new Instance(name, ValuedPropertyIO.toDomainInstances(valuedProperties));
    }

    public static List<Instance> toDomainInstances(List<InstanceIO> instanceIOS) {
        return Optional.ofNullable(instanceIOS)
                .orElse(Collections.emptyList())
                .stream()
                .map(InstanceIO::toDomainInstance)
                .collect(Collectors.toList());
    }

    public static List<InstanceIO> fromInstanceViews(List<InstanceView> instanceViews) {
        return Optional.ofNullable(instanceViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(InstanceIO::new)
                .collect(Collectors.toList());
    }
}
