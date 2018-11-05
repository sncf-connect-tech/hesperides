package org.hesperides.core.presentation.io.events;

import lombok.Value;
import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.modules.*;
import org.hesperides.core.domain.platforms.PlatformCreatedEvent;
import org.hesperides.core.domain.platforms.PlatformDeletedEvent;
import org.hesperides.core.domain.platforms.PlatformUpdatedEvent;
import org.hesperides.core.domain.security.UserEvent;
import org.hesperides.core.domain.technos.*;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hesperides.core.domain.templatecontainers.entities.TemplateContainer.VersionType.workingcopy;


@Value
public class EventOutput {

    String type;
    Long timestamp;
    String user;
    Map<String, Object> data;

    public EventOutput(EventView view) {
        this.type = view.getType();
        this.timestamp = view.getTimestamp().toEpochMilli();
        this.user = view.getData().getUser().getName();
        this.data = getEventData(view.getData());
    }

    /*
     * TODO: Map future new events here based on Angular directive in front
     * Some event does not require a mapping, but we prefer set them here even if there's nothing to do
     * https://github.com/voyages-sncf-technologies/hesperides-gui/tree/master/src/app/event/directives
     */
    private static Map<String, Object> getEventData(final UserEvent userEvent) {
        final Map<String, Object> eventData = new HashMap<>();
        if (userEvent instanceof ModuleCreatedEvent) {
            ModuleCreatedEvent e = (ModuleCreatedEvent) userEvent;
            Map<String, Object> moduleCreated = new HashMap<>();
            moduleCreated.put("name", e.getModule().getKey().getName());
            moduleCreated.put("version", e.getModule().getKey().getVersion());
            moduleCreated.put("working_copy", e.getModule().getKey().getVersionType() == workingcopy);
            eventData.put("moduleCreated", moduleCreated);
        } else if (userEvent instanceof TemplateCreatedEvent) {
            TemplateCreatedEvent e = (TemplateCreatedEvent) userEvent;
            eventData.put("created", singletonMap("name", e.getTemplate().getName()));
        } else if (userEvent instanceof TemplateUpdatedEvent) {
            TemplateUpdatedEvent e = (TemplateUpdatedEvent) userEvent;
            eventData.put("updated", singletonMap("name", e.getTemplate().getName()));
        } else if (userEvent instanceof TemplateDeletedEvent) {
            TemplateDeletedEvent e = (TemplateDeletedEvent) userEvent;
            eventData.put("templateName", e.getTemplateName());
        }
        return eventData;
    }
}
