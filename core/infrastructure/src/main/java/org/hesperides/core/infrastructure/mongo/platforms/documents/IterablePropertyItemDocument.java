package org.hesperides.core.infrastructure.mongo.platforms.documents;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.core.domain.platforms.entities.properties.IterablePropertyItem;
import org.hesperides.core.domain.platforms.queries.views.properties.IterablePropertyItemView;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Document
@NoArgsConstructor
public class IterablePropertyItemDocument {

    String title;
    private List<AbstractValuedPropertyDocument> abstractValuedProperties;

    public IterablePropertyItemDocument(final IterablePropertyItem iterablePropertyItem) {
        title = iterablePropertyItem.getTitle();
        abstractValuedProperties = AbstractValuedPropertyDocument.fromAbstractDomainInstances(iterablePropertyItem.getAbstractValuedProperties());
    }

    public static List<IterablePropertyItemView> toIterablePropertyItemViews(final List<IterablePropertyItemDocument> iterablePropertyItems) {
        return Optional.ofNullable(iterablePropertyItems)
                .orElse(Collections.emptyList())
                .stream()
                .map(IterablePropertyItemDocument::toIterablePropertyItemView)
                .collect(Collectors.toList());
    }

    public static IterablePropertyItemView toIterablePropertyItemView(final IterablePropertyItemDocument iterablePropertyItemDocument) {
        return new IterablePropertyItemView(iterablePropertyItemDocument.getTitle(),
                AbstractValuedPropertyDocument.toAbstractValuedPropertyViews(iterablePropertyItemDocument.getAbstractValuedProperties()));
    }

    public static List<IterablePropertyItem> toDomainInstances(List<IterablePropertyItemDocument> iterablePropertyItemDocuments) {
        return Optional.ofNullable(iterablePropertyItemDocuments)
                .orElse(Collections.emptyList())
                .stream()
                .map(IterablePropertyItemDocument::toDomainInstance)
                .collect(Collectors.toList());
    }

    public static IterablePropertyItem toDomainInstance(IterablePropertyItemDocument iterablePropertyItemDocument) {
        return new IterablePropertyItem(
                iterablePropertyItemDocument.title,
                AbstractValuedPropertyDocument.toAbstractDomainInstances(iterablePropertyItemDocument.abstractValuedProperties)
        );
    }

    public static List<IterablePropertyItemDocument> fromDomainInstances(List<IterablePropertyItem> items) {
        return Optional.ofNullable(items)
                .orElse(Collections.emptyList())
                .stream()
                .map(IterablePropertyItemDocument::new)
                .collect(Collectors.toList());
    }
}
