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

import java.security.InvalidParameterException;
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
    private static final String INVALID_STREAM_NAME_ERROR_MSG = "Invalid stream name, expected format: module-$name-$version-$versionType or platform-$app-$platform";

    private final EventQueries eventQueries;
    private final ModuleQueries moduleQueries;

    @Autowired
    public EventUseCases(EventQueries eventQueries, ModuleQueries moduleQueries) {
        this.eventQueries = eventQueries;
        this.moduleQueries = moduleQueries;
    }

    public List<EventView> parseStreamNameAndGetEvents(String streamNameWithType, Integer page, Integer size) {
        String[] split = streamNameWithType.split("-", 2);
        if (split.length < 2) {
            throw new IllegalArgumentException(INVALID_STREAM_NAME_ERROR_MSG);
        }
        String streamType = split[0];
        String streamName = split[1];
        switch (streamType) {
            case MODULE_EVENTSTREAM_TYPE:
                return moduleQueries.getModulesName()
                        .stream()
                        .filter(m -> contains(streamName, m)) // Lucas 2019/02/28: peu robuste, on peut avoir la cas de modules toto & toto-tata => toto peut être sélectionné ici alors qu'on requête toto-tata
                        .findFirst()
                        .map(moduleKey -> parseModuleKey(streamName, moduleKey))
                        .map(key -> getEvents(key, page, size))
                        .orElse(Collections.emptyList());
            case PLATFORM_EVENTSTREAM_TYPE:
                String[] streamNameSplited = streamName.split("-", 2);
                if (streamNameSplited.length < 2) {
                    throw new IllegalArgumentException(INVALID_STREAM_NAME_ERROR_MSG);
                }
                final Platform.Key platformKey = new Platform.Key(streamNameSplited[0], streamNameSplited[1]);
                return getEvents(platformKey, page, size);
            default:
                throw new IllegalArgumentException(INVALID_STREAM_NAME_ERROR_MSG);
        }
    }

    @NotNull
    private static Module.Key parseModuleKey(String streamName, String moduleName) {
        String versionAndVersionType = streamName.replace(moduleName + "-", "");
        Matcher matcher = Pattern.compile("(.*)-([^-]+)$").matcher(versionAndVersionType);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(INVALID_STREAM_NAME_ERROR_MSG);
        }
        TemplateContainer.VersionType versionType;
        try {
            versionType = TemplateContainer.VersionType.fromMinimizedForm(matcher.group(2));
        } catch(InvalidParameterException e) {
            throw new IllegalArgumentException(INVALID_STREAM_NAME_ERROR_MSG);
        }
        return new Module.Key(moduleName, matcher.group(1), versionType);
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
