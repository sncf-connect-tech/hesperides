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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(callSuper = true)
public class ValuedPropertyView extends AbstractValuedPropertyView {

    public final static String OBFUSCATED_PASSWORD_VALUE = "********";

    String mustacheContent;
    String value;
    String defaultValue;
    boolean isPassword;

    public ValuedPropertyView(String mustacheContent, String name, String value, String defaultValue, boolean isPassword) {
        super(name);
        this.mustacheContent = mustacheContent;
        this.value = value;
        this.defaultValue = defaultValue;
        this.isPassword = isPassword;
    }

    @Override
    public ValuedProperty toDomainValuedProperty() {
        return new ValuedProperty(mustacheContent, getName(), value, defaultValue, isPassword);
    }

    @Override
    public AbstractValuedPropertyView withPasswordsHidden() {
        AbstractValuedPropertyView valuedProperty = this;
        if (isPassword) {
            valuedProperty = new ValuedPropertyView(mustacheContent, getName(), OBFUSCATED_PASSWORD_VALUE, defaultValue, isPassword);
        }
        return valuedProperty;
    }

    @Override
    protected Optional<AbstractValuedPropertyView> getOnlyValuedProperty() {
        return StringUtils.isEmpty(value) ? Optional.empty() : Optional.of(this);
    }

    public static List<ValuedProperty> toDomainValuedProperties(List<ValuedPropertyView> valuedProperties) {
        return Optional.ofNullable(valuedProperties)
                .orElse(Collections.emptyList())
                .stream()
                .map(ValuedPropertyView::toDomainValuedProperty)
                .collect(Collectors.toList());
    }
}
