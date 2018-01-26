package org.hesperides.domain.modules.commands;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.domain.modules.Module;
import org.hesperides.domain.modules.events.ModuleCopiedEvent;
import org.hesperides.domain.modules.events.ModuleCreatedEvent;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

/**
 * this is the main module aggregate
 */
@Slf4j
@Aggregate
public class ModuleAggregate {

    @AggregateIdentifier
    Module.Key key;

    public ModuleAggregate() {}

    @CommandHandler
    public ModuleAggregate(CreateModuleCommand command) {
        apply(new ModuleCreatedEvent(command.getModuleKey()));
    }

    @CommandHandler
    public ModuleAggregate(CopyModuleCommand command) {
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
