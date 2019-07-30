package org.hesperides.core.domain.technos.exception;

import org.hesperides.core.domain.exceptions.DuplicateException;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.TemplateContainerKeyView;

import java.util.List;
import java.util.stream.Collectors;

public class UndeletableTechnoInUseException extends DuplicateException {
    public UndeletableTechnoInUseException(TemplateContainer.Key technoKey, List<TemplateContainerKeyView> technoModulesViews) {
        super("Techno " + technoKey.getNamespaceWithoutPrefix() + " cannot be deleted as it is used by: "
                + technoModulesViews.stream().map(TemplateContainerKeyView::toString).collect(Collectors.joining(" - ")));
    }
}
