package org.hesperides.domain.modules;

import lombok.Value;

@Value
public class ModuleReleasedEvent {
    String name;
    String version;
}
