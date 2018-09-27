package org.hesperides.core.infrastructure.mongo.platforms.documents;

import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.IterablePropertyItem;
import org.hesperides.core.domain.platforms.entities.properties.IterableValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.properties.IterablePropertyItemView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
public class IterablePropertyItemDocument {

    String title;
    private List<AbstractValuedPropertyDocument> iterableValuedPropertyDocument;

    public static List<IterablePropertyItemView> toIterablePropertyItemView(final List<IterablePropertyItemDocument> iterablePropertyItems) {
        return Optional.ofNullable(iterablePropertyItems)
                .orElse(Collections.emptyList())
                .stream()
                .map(IterablePropertyItemDocument::IterablePropertyItemView)
                .collect(Collectors.toList());
    }

    private static IterablePropertyItemView IterablePropertyItemView(final IterablePropertyItemDocument iterablePropertyItemDocument) {
        return new IterablePropertyItemView(iterablePropertyItemDocument.getTitle(),
                AbstractValuedPropertyDocument.toAbstractValuedPropertyViews(iterablePropertyItemDocument.getIterableValuedPropertyDocument()));
    }

    public static IterablePropertyItemDocument fromDomainInstance(final IterablePropertyItem iterablePropertyItem) {
        List<AbstractValuedPropertyDocument> abstractValuedPropertyDocuments = new ArrayList<>();

        List<ValuedProperty> valuedPropertyDocuments = AbstractValuedProperty.filterAbstractValuedPropertyWithType(iterablePropertyItem.getAbstractValuedProperties(), ValuedProperty.class);
        abstractValuedPropertyDocuments.addAll(ValuedPropertyDocument.fromDomainInstances(valuedPropertyDocuments));

        List<IterableValuedProperty> iterableValuedProperties = AbstractValuedProperty.filterAbstractValuedPropertyWithType(iterablePropertyItem.getAbstractValuedProperties(), IterableValuedProperty.class);
        abstractValuedPropertyDocuments.addAll(IterableValuedPropertyDocument.fromDomainInstances(iterableValuedProperties));

        return new IterablePropertyItemDocument(iterablePropertyItem.getTitle(), abstractValuedPropertyDocuments);
    }
}
