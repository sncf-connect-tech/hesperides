package org.hesperides.core.infrastructure.mongo.platforms.documents;

import lombok.Value;
import org.hesperides.core.domain.platforms.queries.views.properties.IterablePropertyItemView;

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
}
