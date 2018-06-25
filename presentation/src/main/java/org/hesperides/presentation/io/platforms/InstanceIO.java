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
package org.hesperides.presentation.io.platforms;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.platforms.entities.Instance;
import org.hesperides.domain.platforms.queries.views.InstanceView;
import org.hesperides.presentation.io.platforms.properties.ValorisedPropertyIO;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class InstanceIO {

    String name;
    @SerializedName("key_values")
    List<ValorisedPropertyIO> valorisedProperties;

    public static List<Instance> toDomainInstances(List<InstanceIO> instanceIOS) {
        List<Instance> instances = null;
        if (instanceIOS != null) {
            instances = instanceIOS.stream().map(InstanceIO::toDomainInstance).collect(Collectors.toList());
        }
        return instances;
    }

    public static List<InstanceIO> fromInstanceViews(List<InstanceView> instanceViews) {
        List<InstanceIO> instanceIOS = null;
        if (instanceViews != null) {
            instanceIOS = instanceViews.stream().map(InstanceIO::fromInstanceView).collect(Collectors.toList());
        }
        return instanceIOS;
    }

    public static InstanceIO fromInstanceView(InstanceView instanceView) {
        return new InstanceIO(
                instanceView.getName(),
                ValorisedPropertyIO.fromPropertyViews(instanceView.getValorisedProperties())
        );
    }

    public Instance toDomainInstance() {
        return new Instance(name, ValorisedPropertyIO.toDomainInstances(valorisedProperties));
    }
}
