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
package org.hesperides.core.infrastructure.mongo.templatecontainers;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hesperides.core.domain.templatecontainers.entities.IterableProperty;
import org.hesperides.core.domain.templatecontainers.queries.IterablePropertyView;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document
public class IterablePropertyDocument extends AbstractPropertyDocument {

    private List<AbstractPropertyDocument> properties;

    public IterablePropertyDocument(IterableProperty iterableProperty) {
        this.name = iterableProperty.getName();
        this.properties = AbstractPropertyDocument.fromDomainInstances(iterableProperty.getProperties());
    }

    @Override
    public IterableProperty toDomainInstance() {
        return new IterableProperty(name, AbstractPropertyDocument.toDomainInstances(properties));
    }

    @Override
    public IterablePropertyView toView() {
        return new IterablePropertyView(getName(), AbstractPropertyDocument.toViews(properties));
    }

    @Override
    protected Stream<PropertyDocument> flattenProperties() {
        return Optional.ofNullable(properties)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(AbstractPropertyDocument::flattenProperties)
                .flatMap(Function.identity());
    }
}
