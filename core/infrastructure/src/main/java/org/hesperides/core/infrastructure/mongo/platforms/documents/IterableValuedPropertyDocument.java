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
import org.hesperides.core.domain.platforms.entities.properties.IterableValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;
import org.hesperides.core.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.IterablePropertyDocument;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document
public class IterableValuedPropertyDocument extends AbstractValuedPropertyDocument {

    private List<IterablePropertyItemDocument> items;

    public IterableValuedPropertyDocument(final IterableValuedProperty iterableValuedProperty) {
        name = iterableValuedProperty.getName();
        items = IterablePropertyItemDocument.fromDomainInstances(iterableValuedProperty.getItems());
    }

    public IterableValuedPropertyView toView() {
        return new IterableValuedPropertyView(getName(), IterablePropertyItemDocument.toIterablePropertyItemViews(items));
    }

    @Override
    protected IterableValuedProperty toDomainInstance() {
        return new IterableValuedProperty(name, IterablePropertyItemDocument.toDomainInstances(items));
    }

    @Override
    protected List<AbstractValuedPropertyDocument> completeWithMustacheContent(List<AbstractPropertyDocument> abstractModelProperties) {
        List<AbstractPropertyDocument> moduleIterablePropertyChildren = abstractModelProperties.stream()
                .filter(IterablePropertyDocument.class::isInstance)
                .map(IterablePropertyDocument.class::cast)
                .filter(iterablePropertyDocument -> name.equals(iterablePropertyDocument.getName()))
                .findFirst()
                .map(IterablePropertyDocument::getProperties)
                .orElse(Collections.emptyList());

        List<IterablePropertyItemDocument> completedItems = new ArrayList<>();
        this.items.forEach(item -> {
            IterablePropertyItemDocument newItem = new IterablePropertyItemDocument();
            newItem.setTitle(item.getTitle());
            // Récursivité
            newItem.setAbstractValuedProperties(completePropertiesWithMustacheContent(item.getAbstractValuedProperties(), moduleIterablePropertyChildren));
            completedItems.add(newItem);
        });
        this.items = completedItems;
        // On ne retourne toujours qu'une seule propriété itérable mais sous forme de liste
        // car la méthode abstraite, elle, doit retourner une liste à cause du cas de propriété
        // simple définie plusieurs fois avec le même nom mais un commentaire distinct.
        /** @see org.hesperides.core.infrastructure.mongo.platforms.documents.ValuedPropertyDocument#completeWithMustacheContent */
        return Arrays.asList(this);
    }
}
