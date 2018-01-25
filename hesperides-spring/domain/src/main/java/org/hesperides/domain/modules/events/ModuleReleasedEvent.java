package org.hesperides.domain.modules.events;

import lombok.Value;

@Value
public class ModuleReleasedEvent {
    String name;
    String version;
}
