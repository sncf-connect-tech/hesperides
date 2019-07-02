package org.hesperides.core.domain.technos.exception;

import org.hesperides.core.domain.exceptions.DuplicateException;
import org.hesperides.core.domain.templatecontainers.queries.KeyView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import java.util.List;
import java.util.stream.Collectors;

public class UndeletableTechnoInUseException extends DuplicateException {
    public UndeletableTechnoInUseException(TemplateContainer.Key technoKey, List<KeyView> technoModulesViews) {
        super("Techno " + technoKey.getNamespaceWithoutPrefix() + " cannot be deleted as it is used by: "
              + technoModulesViews.stream().map(KeyView::toString).collect(Collectors.joining( " - " )));
    }
}
