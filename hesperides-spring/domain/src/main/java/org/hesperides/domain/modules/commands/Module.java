package org.hesperides.domain.modules.commands;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.domain.modules.ModuleType;
import org.hesperides.domain.modules.events.ModuleCopiedEvent;
import org.hesperides.domain.modules.events.ModuleCreatedEvent;

import java.net.URI;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

/**
 * this is the main module aggregate
 */
@Slf4j
@Aggregate
public class Module {

    @Value
    public static class Key {
        String name;
        String version;
        ModuleType versionType;

        @JsonIgnore
        public URI getURI() {
            return URI.create("/rest/modules/" + name + "/" + version + "/" + versionType.name().toLowerCase());
        }
    }

    @AggregateIdentifier
    Key key;

    public Module() {}

    @CommandHandler
    public Module(CreateModuleCommand command) {
        apply(new ModuleCreatedEvent(command.getModuleKey()));
    }

    @CommandHandler
    public Module(CopyModuleCommand command) {
        apply(new ModuleCopiedEvent(command.getModuleKey(), command.getSourceModuleKey()));
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void on(ModuleCreatedEvent event) {
        this.key = event.getModuleKey();
        log.debug("module créé.");
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void on(ModuleCopiedEvent event) {
        this.key = event.getModuleKey();
        // set les trucs du module en copiant depuis l'event.
        log.debug("module copié.");
    }
}
