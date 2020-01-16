package org.hesperides.core.application.platforms.properties.mappers;


import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.platforms.PlatformModulePropertiesUpdatedEvent;
import org.hesperides.core.domain.platforms.entities.properties.events.PropertiesEvent;

public class PropertiesEventMapper {


    // TODO pour faire le diff des props, il faudra comparer deux evènements
    public static PropertiesEvent convertToPropertiesEvent(final EventView event) {
        final PlatformModulePropertiesUpdatedEvent modulePropsEvent = (PlatformModulePropertiesUpdatedEvent) event.getData();

        return PropertiesEvent.builder()
                .author(modulePropsEvent.getUser())
                .timestamp(event.getTimestamp())
                .build();
    }
}
