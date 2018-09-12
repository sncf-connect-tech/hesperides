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
package org.hesperides.core.infrastructure.mongo.platforms.documents;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterablePropertyItemView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document
public class IterableValuedPropertyDocument extends AbstractValuedPropertyDocument {

    private List<IterablePropertyItemDocument> iterablePropertyItems;

    public IterableValuedPropertyView toIterableValuedPropertyView() {
        return new IterableValuedPropertyView(getName(), IterablePropertyItemDocument.toIterablePropertyItemView(iterablePropertyItems));
    }

    public static List<IterableValuedPropertyView> toIterableValuedPropertyViews(final List<IterableValuedPropertyDocument> iterableValuedPropertyDocuments) {
        return Optional.ofNullable(iterableValuedPropertyDocuments)
                .orElse(Collections.emptyList())
                .stream()
                .map(IterableValuedPropertyDocument::toIterableValuedPropertyView)
                .collect(Collectors.toList());
    }


}
