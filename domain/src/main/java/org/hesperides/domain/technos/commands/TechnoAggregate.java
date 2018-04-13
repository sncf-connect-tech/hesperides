package org.hesperides.domain.technos.commands;

import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateMember;
import org.hesperides.domain.modules.CreateModuleCommand;
import org.hesperides.domain.modules.ModuleCreatedEvent;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@NoArgsConstructor
class TechnoAggregate implements Serializable {
    @AggregateIdentifier
    private Techno.Key key;
    @AggregateMember
    private Map<String, Template> templates = new HashMap<>();

    @CommandHandler
    public ModuleAggregate(CreateModuleCommand command) {
        log.debug("Applying create module command...");
        // Initialise le version_id
        Module module = new Module(
                command.getModule().getKey(),
                command.getModule().getTechnos(),
                1L);
        apply(new ModuleCreatedEvent(module, command.getUser()));
    }
}
