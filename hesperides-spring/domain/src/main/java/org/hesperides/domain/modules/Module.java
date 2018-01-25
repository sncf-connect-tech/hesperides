package org.hesperides.domain.modules;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventhandling.EventHandler;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

/**
 * this is the main module aggregate
 */
public class Module {

    @AggregateIdentifier
    private String name;

    private String version;

    public Module() {}

    @CommandHandler
    public Module(CreateModuleCommand command) {
        apply(new ModuleCreatedEvent(command.getName()));
    }

    @CommandHandler
    public void releaseModule(ReleaseModuleCommand command) {
        apply(new ModuleReleasedEvent(this.name, command.getNewVersion()));
    }

    @EventHandler
    public void on(ModuleReleasedEvent event) {
        this.version = event.getVersion();
    }

    @EventHandler
    public void on(ModuleCreatedEvent event) {
        this.name = event.getName();
        this.version = "SNAPSHOT";
    }
}
