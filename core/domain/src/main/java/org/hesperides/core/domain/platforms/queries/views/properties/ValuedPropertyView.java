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

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(callSuper = true)
public class ValuedPropertyView extends AbstractValuedPropertyView {

    public final static String OBFUSCATED_PASSWORD_VALUE = "********";

    String value;

    public ValuedPropertyView(String name, String value) {
        super(name);
        this.value = value;
    }

    @Override
    public ValuedProperty toDomainValuedProperty() {
        return new ValuedProperty(getName(), value);
    }

    @Override
    public AbstractValuedPropertyView withPasswordsHidden(Predicate<String> isPassword) {
        AbstractValuedPropertyView valuedProperty = this;
        if (isPassword.test(getName())) {
            valuedProperty = new ValuedPropertyView(getName(), OBFUSCATED_PASSWORD_VALUE);
        }
        return valuedProperty;
    }

    @Override
    protected Optional<AbstractValuedPropertyView> excludePropertyWithOnlyDefaultValue(AbstractPropertyView propertyModel) {
        if (StringUtils.isNotEmpty(value)) {
            return Optional.of(this);
        }
        PropertyView singlePropertyModel = (PropertyView) propertyModel;
        return singlePropertyModel == null || StringUtils.isEmpty(singlePropertyModel.getDefaultValue()) ? Optional.of(this) : Optional.empty();
    }

    public static List<ValuedProperty> toDomainValuedProperties(List<ValuedPropertyView> valuedProperties) {
        return Optional.ofNullable(valuedProperties)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(ValuedPropertyView::toDomainValuedProperty)
                .collect(Collectors.toList());
    }

    public ValuedPropertyView withValue(String value) {
        return new ValuedPropertyView(getName(), value);
    }
}
