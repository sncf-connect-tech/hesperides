package org.hesperides.domain.modules.commands;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.domain.modules.Module;
import org.hesperides.domain.modules.Template;
import org.hesperides.domain.modules.events.ModuleCopiedEvent;
import org.hesperides.domain.modules.events.ModuleCreatedEvent;
import org.hesperides.domain.modules.events.TemplateCreatedEvent;
import org.hesperides.domain.modules.events.TemplateDeletedEvent;
import org.hesperides.domain.modules.exceptions.DuplicateTemplateCreationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;
import static org.axonframework.commandhandling.model.AggregateLifecycle.isLive;

/**
 * this is the main module aggregate
 */
@Slf4j
@Aggregate
public class ModuleAggregate {

    @AggregateIdentifier
    Module.Key key;

    Map<String, Template> templates = new HashMap<>();

    public ModuleAggregate() {
    }

    @CommandHandler
    public ModuleAggregate(CreateModuleCommand command) {
        apply(new ModuleCreatedEvent(command.getModuleKey()));
    }

    @CommandHandler
    public ModuleAggregate(CopyModuleCommand command) {
        apply(new ModuleCopiedEvent(command.getModuleKey(), command.getSourceModuleKey()));
    }

    @CommandHandler
    public void createTemplate(CreateTemplateCommand command) {
        log.debug("Applying create template command...");

        // check qu'on a pas déjà un template avec ce nom, sinon erreur:
        if (this.templates.containsKey(command.getTemplate().getName())) {
            throw new DuplicateTemplateCreationException(command.getTemplate());
        }

        apply(new TemplateCreatedEvent(key, command.getTemplate()));
    }

    @CommandHandler
    public void deleteTemplate(DeleteTemplateCommand command) {
        // si le template n'existe pas, cette command n'a pas d'effet de bord.
        if (this.templates.containsKey(command.getTemplateName())) {
            apply(new TemplateDeletedEvent(key, command.getTemplateName()));
        }
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    private void on(ModuleCreatedEvent event) {
        this.key = event.getModuleKey();
        log.debug("module créé. (aggregate is live ? {})", isLive());
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    private void on(ModuleCopiedEvent event) {
        this.key = event.getModuleKey();
        // set les trucs du module en copiant depuis l'event.
        log.debug("module copié.");
    }

    @EventSourcingHandler
    private void on(TemplateCreatedEvent event) {
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
        log.debug("template ajouté. ");
    }

    @EventSourcingHandler
    private void on(TemplateDeletedEvent event) {
        this.templates.remove(event.getTemplateName());
        log.debug("template ajouté. ");
    }
}
