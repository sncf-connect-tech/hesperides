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
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;

import static java.util.stream.Collectors.*;
import static java.util.function.Function.identity;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Value
@NonFinal
public abstract class AbstractValuedPropertyView {

    String name;

    public abstract <T extends AbstractValuedProperty> T toDomainValuedProperty();

    protected abstract AbstractValuedPropertyView withPasswordsHidden(Predicate<String> isPassword);

    protected abstract Optional<AbstractValuedPropertyView> excludePropertyWithOnlyDefaultValue(Map<String, AbstractPropertyView> modelPerName);

    protected abstract Optional<? extends AbstractValuedPropertyView> excludeUnusedValue(
            Map<String, AbstractPropertyView> propertiesPerName, Set<String> referencedProperties);

    public static List<AbstractValuedProperty> toDomainAbstractValuedProperties(List<AbstractValuedPropertyView> valuedProperties) {
        return Optional.ofNullable(valuedProperties)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(AbstractValuedPropertyView::toDomainValuedProperty)
                .map(AbstractValuedProperty.class::cast)
                .collect(toList());
    }

    public static <T extends AbstractValuedPropertyView> List<T> getAbstractValuedPropertyViewWithType(final List<AbstractValuedPropertyView> abstractValuedPropertyViews, Class<T> clazz) {
        return Optional.ofNullable(abstractValuedPropertyViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(toList());
    }

    public static List<AbstractValuedPropertyView> hidePasswordProperties(List<AbstractValuedPropertyView> valuedProperties, List<AbstractPropertyView> propertiesModel) {
        // Legacy reference implementation: https://github.com/voyages-sncf-technologies/hesperides/blob/fix/3.0.3/src/main/java/com/vsct/dt/hesperides/resources/PermissionAwareApplicationsProxy.java#L288
        Set<String> passwordPropertyNames = AbstractPropertyView.getAllSimpleProperties(propertiesModel)
                .filter(PropertyView::isPassword)
                .map(PropertyView::getName)
                .collect(toSet());

        return valuedProperties.stream()
                .map(property -> property.withPasswordsHidden(passwordPropertyNames::contains))
                .collect(toList());
    }

    /**
     * Récupère de manière récursive les propriétés en excluant les propriétés valorisées
     * mais n'étant plus
     * <ul>
     * <li>ni définie dans le modèle qu'elles illustrent.
     * <li>ni utilisée par une autre valorisation (incluant les variables d'instances ou itérables)
     * </ul>
     *
     * @param valuedProperties     ensemble de valorisation candidates au nettoyage
     * @param propertiesModel      structure des propriétés dans le module déclarant
     * @param referencedProperties noms des propriétés référencées dans l'ensemble des valorisations (instances comprises),
     *                             utilisés pour évaluer le 2e critère
     */
    public static Stream<AbstractValuedPropertyView> excludeUnusedValues(List<AbstractValuedPropertyView> valuedProperties,
                                                                         List<AbstractPropertyView> propertiesModel,
                                                                         Set<String> referencedProperties) {
        Map<String, AbstractPropertyView> propertiesPerName = indexPropertiesPerName(propertiesModel);

        return valuedProperties.stream()
                .map(valuedProperty -> valuedProperty.excludeUnusedValue(propertiesPerName, referencedProperties))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    /**
     * Récupère de manière récursive les propriétés valorisées en excluant
     * les propriétés non valorisées mais ayant une valeur par défaut.
     * <p>
     * Il y a peut-être moyen de faire ça directement en Mongo mais je ne sais pas comment :)
     */
    public static List<AbstractValuedPropertyView> excludePropertiesWithOnlyDefaultValue(List<AbstractValuedPropertyView> valuedProperties,
                                                                                         List<AbstractPropertyView> propertiesModel) {
        Map<String, AbstractPropertyView> propertiesPerName = indexPropertiesPerName(propertiesModel);

        return valuedProperties.stream()
                .map(valuedProperty -> valuedProperty.excludePropertyWithOnlyDefaultValue(propertiesPerName))
                .filter(Optional::isPresent).map(Optional::get)
                .collect(toList());
    }

    private static Map<String, AbstractPropertyView> indexPropertiesPerName(List<AbstractPropertyView> propertiesModel) {
        Map<String, AbstractPropertyView> index;

        if (propertiesModel == null || propertiesModel.isEmpty()) {
            index = Collections.emptyMap();

        } else {
            // en cas de doublon, c'est le 1er arrivé qui "gagne"
            final BinaryOperator<AbstractPropertyView> firstWins = (p1, p2) -> p1;

            index = propertiesModel.stream()
                    .collect(toMap(AbstractPropertyView::getName, identity(), firstWins));
        }

        return index;
    }
}