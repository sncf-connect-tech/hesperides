package org.hesperides.core.domain.modules.exceptions;

import org.hesperides.core.domain.platforms.queries.views.ModulePlatformView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import java.util.List;
import java.util.stream.Collectors;

public class ModuleUsedByPlatformsException extends RuntimeException {

    public ModuleUsedByPlatformsException(TemplateContainer.Key moduleKey, List<ModulePlatformView> platforms) {
        super("Could not delete module " + moduleKey.getNamespaceWithoutPrefix() +
                " because it is used by platform(s): " + platforms.stream()
                .map(ModulePlatformView::toString)
                .collect(Collectors.joining(", ")));
    }
}
