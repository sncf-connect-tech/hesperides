package org.hesperides.domain.modules.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.hesperides.domain.modules.events.*;
import org.hesperides.domain.modules.exceptions.DuplicateTemplateCreationException;
import org.hesperides.domain.modules.exceptions.TemplateNotFoundException;

import java.util.HashMap;
import java.util.Map;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;
import static org.axonframework.commandhandling.model.AggregateLifecycle.isLive;

/**
 * Cette classe n'est utilisée que par des Commandes
 * C'est un des principaux agrégats (entité racine)
 * Les Query utilisent des View (représentation de données)
 */
@Slf4j
@Aggregate
@NoArgsConstructor
class ModuleAggregate {
    @AggregateIdentifier
    Module.Key key;
    Map<String, Template> templates = new HashMap<>();

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
    public void updateTemplate(UpdateTemplateCommand command) {
        log.debug("Applying update template command...");

        // check qu'on a déjà un template avec ce nom, sinon erreur:
        if (!this.templates.containsKey(command.getTemplate().getName())) {
            throw new TemplateNotFoundException(key, command.getTemplate().getName());
        }

        apply(new TemplateUpdatedEvent(key, command.getTemplate()));
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
    private void on(TemplateUpdatedEvent event) {
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
        log.debug("template update. ");
    }

    @EventSourcingHandler
    private void on(TemplateDeletedEvent event) {
        this.templates.remove(event.getTemplateName());
        log.debug("template ajouté. ");
    }
}
