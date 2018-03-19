package org.hesperides.domain.modules.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateMember;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.hesperides.domain.modules.exceptions.DuplicateTemplateCreationException;
import org.hesperides.domain.modules.exceptions.TemplateNotFoundException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;
import static org.axonframework.commandhandling.model.AggregateLifecycle.isLive;

/**
 * Un agrégat est l'entité racine d'un groupe d'entités.
 * Cette classe n'est utilisée que par des Commands.
 * Les Queries utilisent des Views (représentation de données).
 */
@Slf4j
@Aggregate
/**
 * Axon utilise le constructeur vide pour créer une instance vide
 * avant de l'initialiser à partir des évènements passés.
 */
@NoArgsConstructor
class ModuleAggregate implements Serializable {
    @AggregateIdentifier
    private Module.Key key;
    @AggregateMember
    private Map<String, Template> templates = new HashMap<>();

    @CommandHandler
    public ModuleAggregate(CreateModuleCommand command) {
        apply(new ModuleCreatedEvent(command.getModule(), command.getUser()));
    }

    @CommandHandler
    public ModuleAggregate(UpdateModuleCommand command) {
        log.debug("Applying update module command...");
        apply(new ModuleUpdatedEvent(command.getModule(), command.getUser()));
    }

    @CommandHandler
    public ModuleAggregate(DeleteModuleCommand command) {
        log.debug("Applying delete module command...");
        apply(new ModuleDeletedEvent(command.getModule(), command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void createTemplate(CreateTemplateCommand command) {
        log.debug("Applying create template command...");

        // check qu'on a pas déjà un template avec ce nom, sinon erreur:
        if (this.templates.containsKey(command.getTemplate().getName())) {
            throw new DuplicateTemplateCreationException(command.getTemplate());
        }

        apply(new TemplateCreatedEvent(key, command.getTemplate(), command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void updateTemplate(UpdateTemplateCommand command) {
        log.debug("Applying update template command...");

        // check qu'on a déjà un template avec ce nom, sinon erreur:
        if (!this.templates.containsKey(command.getTemplate().getName())) {
            throw new TemplateNotFoundException(key, command.getTemplate().getName());
        }

        apply(new TemplateUpdatedEvent(key, command.getTemplate(), command.getUser()));
    }


    @CommandHandler
    @SuppressWarnings("unused")
    public void deleteTemplate(DeleteTemplateCommand command) {
        // si le template n'existe pas, cette command n'a pas d'effet de bord.
        if (this.templates.containsKey(command.getTemplateName())) {
            apply(new TemplateDeletedEvent(key, command.getTemplateName(), command.getUser()));
        }
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    private void on(ModuleCreatedEvent event) {
        this.key = event.getModule().getKey();

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
    @SuppressWarnings("unused")
    private void on(ModuleUpdatedEvent event) {
        this.key = event.getModule().getKey();

        log.debug("module mis à jour. (aggregate is live ? {})", isLive());
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    private void on(ModuleDeletedEvent event) {
        this.key = event.getModule().getKey();

        log.debug("module supprimé. (aggregate is live ? {})", isLive());
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
