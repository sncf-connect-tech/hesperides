package org.hesperides.core.application.events;

import org.hesperides.core.domain.events.queries.EventQueries;
import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.contains;

@Component
public class EventUseCases {

    private static final String MODULE_EVENTSTREAM_TYPE = "module";
    private static final String PLATFORM_EVENTSTREAM_TYPE = "platform";

    private final EventQueries eventQueries;
    private final ModuleQueries moduleQueries;

    @Autowired
    public EventUseCases(EventQueries eventQueries, ModuleQueries moduleQueries) {
        this.eventQueries = eventQueries;
        this.moduleQueries = moduleQueries;
    }

    public List<EventView> parseStreamNameAndGetEvents(String streamNameWithType, Integer page, Integer size) {
        String[] split = streamNameWithType.split("-", 2);
        String streamType = split[0];
        String streamName = split[1];
        switch (streamType) {
            case MODULE_EVENTSTREAM_TYPE:
                return moduleQueries.getModulesName()
                        .stream()
                        .filter(m -> contains(streamName, m))
                        .findFirst()
                        .flatMap(moduleKey -> parseModuleKey(streamName, moduleKey))
                        .map(key -> getEvents(key, page, size))
                        .orElse(Collections.emptyList());
            case PLATFORM_EVENTSTREAM_TYPE:
                String[] streamNameSplited = streamName.split("-", 2);
                final Platform.Key platformKey = new Platform.Key(streamNameSplited[0], streamNameSplited[1]);
                return getEvents(platformKey, page, size);
            default:
                return Collections.emptyList();
        }
    }

    @NotNull
    private static Optional<Module.Key> parseModuleKey(String streamName, String moduleName) {
        String versionAndVersionType = streamName.replace(moduleName + "-", "");
        Matcher matcher = Pattern.compile("(.*)-([^-]+)$").matcher(versionAndVersionType);
        if (matcher.matches()) {
            return Optional.of(new Module.Key(moduleName, matcher.group(1), TemplateContainer.VersionType.fromMinimizedForm(matcher.group(2))));
        }
        return Optional.empty();
    }

    public List<EventView> getEvents(TemplateContainer.Key key, Integer page, Integer size) {
        return moduleQueries.getOptionalModuleId(key)
                .map(moduleId -> eventQueries.getEvents(moduleId, page, size))
                .orElse(Collections.emptyList());
    }

    public List<EventView> getEvents(Platform.Key key, Integer page, Integer size) {
        return eventQueries.getEvents(key, page, size);
    }
}
