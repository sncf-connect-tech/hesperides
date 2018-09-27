package org.hesperides.core.domain.modules.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateMember;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.core.domain.modules.*;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

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
    private Long versionId;

    /*** COMMAND HANDLERS ***/

    @CommandHandler
    @SuppressWarnings("unused")
    public ModuleAggregate(CreateModuleCommand command) {
        log.debug("Applying create module command...");
        Module module = command.getModule().initializeVersionId();
        apply(new ModuleCreatedEvent(module, command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onUpdateModuleTechnosCommand(UpdateModuleTechnosCommand command) {
        log.debug("Applying update module command...");

        Module module = command.getModule()
                .validateIsWorkingCopy()
                .validateVersionId(versionId)
                .incrementVersiondId();

        apply(new ModuleTechnosUpdatedEvent(command.getModuleKey(), module.getTechnos(), module.getVersionId(), command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onDeleteModuleCommand(DeleteModuleCommand command) {
        log.debug("Applying delete module command...");
        apply(new ModuleDeletedEvent(command.getModuleKey(), command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onCreateTemplateCommand(CreateTemplateCommand command) {
        log.debug("Applying create template command...");

        Template template = command.getTemplate()
                .validateNameNotTaken(templates, key)
                .validateProperties()
                .initializeVersionId();

        apply(new TemplateCreatedEvent(key, template, command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onUpdateTemplateCommand(UpdateTemplateCommand command) {
        log.debug("Applying update template command...");

        Template template = command.getTemplate()
                .validateExistingName(templates, key)
                .validateVersionId(getExpectedVersionId(command))
                .validateProperties()
                .incrementVersionId();

        apply(new TemplateUpdatedEvent(key, template, command.getUser()));
    }

    private Long getExpectedVersionId(UpdateTemplateCommand command) {
        return templates.get(command.getTemplate().getName()).getVersionId();
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onDeleteTemplateCommand(DeleteTemplateCommand command) {
        // Si le template n'existe pas, cette commande n'a pas d'effet de bord
        if (!this.templates.containsKey(command.getTemplateName())) {
            throw new TemplateNotFoundException(key, command.getTemplateName());
        }
        apply(new TemplateDeletedEvent(key, command.getTemplateName(), command.getUser()));
    }

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void onModuleCreatedEvent(ModuleCreatedEvent event) {
        this.key = event.getModule().getKey();
        this.versionId = event.getModule().getVersionId();
        log.debug("module créé. (aggregate is live ? {})", isLive());
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void onModuleTechnosUpdatedEvent(ModuleTechnosUpdatedEvent event) {
        this.versionId = event.getVersionId();
        log.debug("module mis à jour. (aggregate is live ? {})", isLive());
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void onModuleDeletedEvent(ModuleDeletedEvent event) {
        //TODO Est-ce qu'on met à jour le versionId ?
        log.debug("module supprimé. (aggregate is live ? {})", isLive());
    }

    @EventSourcingHandler
    public void onTemplateCreatedEvent(TemplateCreatedEvent event) {
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
        log.debug("Template crée. ");
    }

    @EventSourcingHandler
    public void onTemplateUpdatedEvent(TemplateUpdatedEvent event) {
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
        log.debug("Template mis à jour. ");
    }

    @EventSourcingHandler
    public void onTemplateDeletedEvent(TemplateDeletedEvent event) {
        this.templates.remove(event.getTemplateName());
        log.debug("Template supprimé");
    }
}
