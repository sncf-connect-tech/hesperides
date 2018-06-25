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
package org.hesperides.presentation.io.platforms.properties;

import lombok.Value;
import org.hesperides.domain.platforms.entities.properties.ValorisedProperty;
import org.hesperides.domain.platforms.queries.views.properties.ValorisedPropertyView;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class ValorisedPropertyIO extends AbstractValorisedPropertyIO {

    String value;

    public ValorisedPropertyIO(String name, String value) {
        super(name);
        this.value = value;
    }

    public static List<ValorisedProperty> toDomainInstances(List<ValorisedPropertyIO> valorisedPropertyOutputs) {
        List<ValorisedProperty> valorisedProperties = null;
        if (valorisedPropertyOutputs != null) {
            valorisedProperties = valorisedPropertyOutputs.stream().map(ValorisedPropertyIO::toDomainInstance).collect(Collectors.toList());
        }
        return valorisedProperties;
    }

    public static List<ValorisedPropertyIO> fromPropertyViews(List<ValorisedPropertyView> valorisedPropertyViews) {
        List<ValorisedPropertyIO> valorisedPropertyOutputs = null;
        if (valorisedPropertyViews != null) {
            valorisedPropertyOutputs = valorisedPropertyViews.stream().map(ValorisedPropertyIO::fromPropertyView).collect(Collectors.toList());
        }
        return valorisedPropertyOutputs;
    }

    public static ValorisedPropertyIO fromPropertyView(ValorisedPropertyView valorisedPropertyView) {
        return new ValorisedPropertyIO(
                valorisedPropertyView.getName(),
                valorisedPropertyView.getValue()
        );
    }

    public ValorisedProperty toDomainInstance() {
        return new ValorisedProperty(getName(), value);
    }
}
