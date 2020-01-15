package org.hesperides.core.domain.platforms.entities.properties.events;

import lombok.Value;

@Value
public class PropertiesEvent {

    String comment;
    PropertiesEventDiff diff;
    
}
