package org.hesperides.core.domain.technos.exception;

import org.hesperides.core.domain.exceptions.DuplicateException;
import org.hesperides.core.domain.modules.queries.TechnoModuleView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import java.util.List;
import java.util.stream.Collectors;

public class UndeletableTechnoInUseException extends DuplicateException {
    public UndeletableTechnoInUseException(TemplateContainer.Key technoKey, List<TechnoModuleView> technoModulesViews) {
        super("Techno " + technoKey.getNamespaceWithoutPrefix() + " cannot be deleted as it is used by: "
              + technoModulesViews.stream().map(TechnoModuleView::toString).collect(Collectors.joining( " - " )));
    }
}
