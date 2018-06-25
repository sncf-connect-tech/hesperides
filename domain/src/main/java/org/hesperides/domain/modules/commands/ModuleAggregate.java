package org.hesperides.domain.modules.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateMember;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.domain.exceptions.OutOfDateVersionException;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.exceptions.DuplicateTemplateCreationException;
import org.hesperides.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.domain.templatecontainers.entities.AbstractProperty;
import org.hesperides.domain.templatecontainers.entities.Template;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
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
/*
  Axon utilise le constructeur vide pour créer une instance vide
  avant de l'initialiser à partir des évènements passés.
 */
@NoArgsConstructor
class ModuleAggregate implements Serializable {

    @AggregateIdentifier
    private TemplateContainer.Key key;
    @AggregateMember
    private Map<String, Template> templates = new HashMap<>();

    @CommandHandler
    @SuppressWarnings("unused")
    public ModuleAggregate(CreateModuleCommand command) {
        log.debug("Applying create module command...");
        // Initialise le version_id
        Module module = new Module(
                command.getModule().getKey(),
                command.getModule().getTemplates(),
                command.getModule().getTechnos(),
                1L);

        apply(new ModuleCreatedEvent(module, command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void on(UpdateModuleTechnosCommand command) {
        log.debug("Applying update module command...");
        // Met à jour le version_id
        Long updatedVersionId = command.getVersionId() + 1;
        apply(new ModuleTechnosUpdatedEvent(command.getModuleKey(), command.getTechnos(), updatedVersionId, command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void on(DeleteModuleCommand command) {
        log.debug("Applying delete module command...");
        apply(new ModuleDeletedEvent(command.getModuleKey(), command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void on(CreateTemplateCommand command) {
        log.debug("Applying create template command...");

        // check qu'on a pas déjà un template avec ce nom, sinon erreur:
        if (this.templates.containsKey(command.getTemplate().getName())) {
            throw new DuplicateTemplateCreationException(command.getTemplate());
        }

        // Initialise le version_id
        Template newTemplate = new Template(
                command.getTemplate().getName(),
                command.getTemplate().getFilename(),
                command.getTemplate().getLocation(),
                command.getTemplate().getContent(),
                command.getTemplate().getRights(),
                1L,
                command.getModuleKey());

        // Vérifie les propriétés
        List<AbstractProperty> abstractProperties = AbstractProperty.extractPropertiesFromTemplate(newTemplate);
        AbstractProperty.validateProperties(abstractProperties);

        apply(new TemplateCreatedEvent(key, newTemplate, command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void on(UpdateTemplateCommand command) {
        log.debug("Applying update template command...");

        // check qu'on a déjà un template avec ce nom, sinon erreur:
        if (!templates.containsKey(command.getTemplate().getName())) {
            throw new TemplateNotFoundException(key, command.getTemplate().getName());
        }
        // Vérifie que le template n'a été modifié entre temps
        Long expectedVersionId = templates.get(command.getTemplate().getName()).getVersionId();
        Long actualVersionId = command.getTemplate().getVersionId();
        if (!expectedVersionId.equals(actualVersionId)) {
            throw new OutOfDateVersionException(expectedVersionId, actualVersionId);
        }

        // Met à jour le version_id
        Template templateWithUpdatedVersionId = new Template(
                command.getTemplate().getName(),
                command.getTemplate().getFilename(),
                command.getTemplate().getLocation(),
                command.getTemplate().getContent(),
                command.getTemplate().getRights(),
                command.getTemplate().getVersionId() + 1,
                command.getModuleKey());

        // Vérifie les propriétés
        List<AbstractProperty> abstractProperties = AbstractProperty.extractPropertiesFromTemplate(templateWithUpdatedVersionId);
        AbstractProperty.validateProperties(abstractProperties);

        apply(new TemplateUpdatedEvent(key, templateWithUpdatedVersionId, command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void on(DeleteTemplateCommand command) {
        // Si le template n'existe pas, cette commande n'a pas d'effet de bord
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
        //TODO set les trucs du module en copiant depuis l'event.
        log.debug("module copié.");
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    private void on(ModuleTechnosUpdatedEvent event) {
        this.key = event.getModuleKey();
        log.debug("module mis à jour. (aggregate is live ? {})", isLive());
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    private void on(ModuleDeletedEvent event) {
        this.key = event.getModuleKey();
        log.debug("module supprimé. (aggregate is live ? {})", isLive());
    }

    @EventSourcingHandler
    private void on(TemplateCreatedEvent event) {
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
        log.debug("Template crée. ");
    }

    @EventSourcingHandler
    private void on(TemplateUpdatedEvent event) {
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
        log.debug("Template mis à jour. ");
    }

    @EventSourcingHandler
    private void on(TemplateDeletedEvent event) {
        this.templates.remove(event.getTemplateName());
        log.debug("Template supprimé");
    }
}
