/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/sncf-connect-tech/hesperides)
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
package org.hesperides.core.domain.templatecontainers.queries;

import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@Value
@NonFinal
public abstract class AbstractPropertyView {

    String name;

    protected abstract Stream<PropertyView> flattenProperties();

    /**
     * Retourne un stream contenant toutes les propriétés simples,
     * y compris celles des propriétés itérables.
     */
    public static Stream<PropertyView> getAllSimpleProperties(List<AbstractPropertyView> abstractProperties) {
        return abstractProperties.stream()
                .map(AbstractPropertyView::flattenProperties)
                .flatMap(Function.identity());
    }
}
