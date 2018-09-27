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
package org.hesperides.core.presentation.io.templatecontainers;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.IterablePropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Value
@AllArgsConstructor
public class ModelOutput {

    @SerializedName("key_value_properties")
    Set<PropertyOutput> properties;
    @SerializedName("iterable_properties")
    Set<PropertyOutput> iterableProperties;

    /**
     * Le legacy distingue les propriétés qui ont un nom et un commentaire identiques,
     * d'où l'utilisation des Set dans cette classe.
     *
     * @param abstractPropertyViews
     */
    public ModelOutput(List<AbstractPropertyView> abstractPropertyViews) {
        Set<PropertyOutput> propertyOutputs = new HashSet<>();
        Set<PropertyOutput> iterablePropertyOutputs = new HashSet<>();

        if (abstractPropertyViews != null) {
            for (AbstractPropertyView abstractPropertyView : abstractPropertyViews) {
                PropertyOutput propertyOutput = new PropertyOutput(abstractPropertyView);
                if (abstractPropertyView instanceof PropertyView) {
                    propertyOutputs.add(propertyOutput);
                } else if (abstractPropertyView instanceof IterablePropertyView) {
                    iterablePropertyOutputs.add(propertyOutput);
                }
            }
        }

        this.properties = propertyOutputs;
        this.iterableProperties = iterablePropertyOutputs;
    }
}
