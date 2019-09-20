package org.hesperides.core.domain.modules.exceptions;

import org.hesperides.core.domain.exceptions.DuplicateException;
import org.hesperides.core.domain.platforms.queries.views.ModulePlatformView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import java.util.List;
import java.util.stream.Collectors;

public class ConflictModuleException extends DuplicateException {
    public ConflictModuleException(TemplateContainer.Key newModuleKey, List<ModulePlatformView> modulePlatformViews) {
        super("could not delete a module with key: "+ newModuleKey.getNamespaceWithoutPrefix() +
                " as it's used by "+ modulePlatformViews.stream()
                .map(ModulePlatformView::toString).collect(Collectors.joining(" - ")) );
    }
}
