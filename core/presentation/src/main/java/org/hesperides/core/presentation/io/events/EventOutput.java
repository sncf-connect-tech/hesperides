package org.hesperides.core.presentation.io.events;

import lombok.Value;
import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.modules.ModuleCreatedEvent;
import org.hesperides.core.domain.modules.TemplateCreatedEvent;
import org.hesperides.core.domain.modules.TemplateUpdatedEvent;
import org.hesperides.core.domain.security.UserEvent;


@Value
public class EventOutput {

    String type;
    Long timestamp;
    String user;
    Object data;

    public EventOutput(EventView view) {
        this.type = view.getType();
        this.timestamp = view.getTimestamp().getEpochSecond();
        this.user = view.getData().getUser();
        this.data = getEventData(view.getData());
    }

    /*
     * For legacy retro-compatibility, look for usage of event.data in: https://github.com/sncf-connect-tech/hesperides-gui/tree/master/src/app/event/directives
     */
    private static Object getEventData(final UserEvent userEvent) {
        if (userEvent instanceof ModuleCreatedEvent) {
            return new ModuleCreatedEventIO((ModuleCreatedEvent) userEvent);
        }
        if (userEvent instanceof TemplateCreatedEvent) {
            return new TemplateCreatedEventIO((TemplateCreatedEvent) userEvent);
        }
        if (userEvent instanceof TemplateUpdatedEvent) {
            return new TemplateUpdatedEventIO((TemplateUpdatedEvent) userEvent);
        }
        // For TemplateDeletedEvent, only field used by legacy front is .templateName, so we pass through the event
        // For many other events (ModuleTechnosUpdatedEvent, techno events...) legacy front was totally bogus and used .platform.platform_name...
        return userEvent;
    }
}
