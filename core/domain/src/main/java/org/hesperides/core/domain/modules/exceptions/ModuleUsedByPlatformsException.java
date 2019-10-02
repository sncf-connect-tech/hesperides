package org.hesperides.core.domain.modules.exceptions;

import org.hesperides.core.domain.platforms.queries.views.ModulePlatformView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import java.util.List;
import java.util.stream.Collectors;

public class ModuleUsedByPlatformsException extends RuntimeException {

    public ModuleUsedByPlatformsException(TemplateContainer.Key moduleKey, List<ModulePlatformView> modulePlatformViews) {
        super("could not delete a module with key: " + moduleKey.getNamespaceWithoutPrefix() +
                " as it's used by those platforms: " + modulePlatformViews.stream()
                .map(ModulePlatformView::toString).collect(Collectors.joining(" - ")));
    }
}
