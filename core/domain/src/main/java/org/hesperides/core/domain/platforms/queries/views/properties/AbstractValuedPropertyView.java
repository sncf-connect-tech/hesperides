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
package org.hesperides.core.domain.platforms.queries.views.properties;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
@NonFinal
public abstract class AbstractValuedPropertyView {

    String name;

    public abstract <T extends AbstractValuedProperty> T toDomainValuedProperty();

    public abstract AbstractValuedPropertyView withPasswordsHidden();

    public static List<AbstractValuedProperty> toDomainAbstractValuedProperties(List<AbstractValuedPropertyView> valuedProperties) {
        return Optional.ofNullable(valuedProperties)
                .orElse(Collections.emptyList())
                .stream()
                .map(AbstractValuedPropertyView::toDomainValuedProperty)
                .map(AbstractValuedProperty.class::cast)
                .collect(Collectors.toList());
    }

    public static <T extends AbstractValuedPropertyView> List<T> getAbstractValuedPropertyViewWithType(final List<AbstractValuedPropertyView> abstractValuedPropertyViews, Class<T> clazz) {
        return Optional.ofNullable(abstractValuedPropertyViews)
                .orElse(Collections.emptyList())
                .stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    public static List<AbstractValuedPropertyView> hidePasswordProperties(List<AbstractValuedPropertyView> moduleProperties) {
        // Legacy reference implementation: https://github.com/voyages-sncf-technologies/hesperides/blob/fix/3.0.3/src/main/java/com/vsct/dt/hesperides/resources/PermissionAwareApplicationsProxy.java#L288
        return moduleProperties.stream().map(AbstractValuedPropertyView::withPasswordsHidden).collect(Collectors.toList());
    }

    /**
     * Récupère de manière récursive les propriétés valorisées en excluant
     * les propriétés non valorisées mais ayant une valeur par défaut.
     * <p>
     * Il y a peut-être moyen de faire ça directement en Mongo mais je ne sais pas comment :)
     */
    public static List<AbstractValuedPropertyView> excludePropertiesWithOnlyDefaultValue(List<AbstractValuedPropertyView> properties) {
        return properties.stream()
                .map(AbstractValuedPropertyView::excludePropertyWithOnlyDefaultValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    protected abstract Optional<AbstractValuedPropertyView> excludePropertyWithOnlyDefaultValue();
}