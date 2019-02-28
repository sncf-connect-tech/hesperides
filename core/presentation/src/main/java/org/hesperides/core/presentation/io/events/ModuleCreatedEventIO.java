package org.hesperides.core.presentation.io.events;

import lombok.Value;
import org.hesperides.core.domain.modules.ModuleCreatedEvent;
import org.hesperides.core.domain.modules.entities.Module;

@Value
public class ModuleCreatedEventIO {
    private Module moduleCreated;  // only field used by legacy front is .name

    public ModuleCreatedEventIO(ModuleCreatedEvent moduleCreatedEvent) {
        this.moduleCreated = moduleCreatedEvent.getModule();
    }
}
