package org.hesperides.core.domain.technos.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateMember;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.core.domain.technos.*;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;
import static org.axonframework.commandhandling.model.AggregateLifecycle.isLive;

@NoArgsConstructor
@Slf4j
@Aggregate
class TechnoAggregate implements Serializable {

    @AggregateIdentifier
    private TemplateContainer.Key key;
    @AggregateMember
    private Map<String, Template> templates = new HashMap<>();

    /*** COMMAND HANDLERS ***/

    @CommandHandler
    @SuppressWarnings("unused")
    public TechnoAggregate(CreateTechnoCommand command) {
        log.debug("Applying CreateTechnoCommand...");
        apply(new TechnoCreatedEvent(command.getTechno(), command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onDeleteTechnoCommand(DeleteTechnoCommand command) {
        log.debug("Applying delete techno command...");
        apply(new TechnoDeletedEvent(command.getTechnoKey(), command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onAddTemplateToTechnoCommand(AddTemplateToTechnoCommand command) {
        log.debug("Applying AddTemplateToTechnoCommand...");

        Template template = command.getTemplate()
                .validateNameNotTaken(templates, key)
                .validateProperties()
                .initializeVersionId();

        apply(new TemplateAddedToTechnoEvent(command.getTechnoKey(), template, command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onUpdateTechnoTemplateCommand(UpdateTechnoTemplateCommand command) {
        log.debug("Applying update template command...");

        Long expectedVersionId = templates.get(command.getTemplate().getName()).getVersionId();

        Template template = command.getTemplate()
                .validateExistingName(templates, key)
                .validateVersionId(expectedVersionId)
                .validateProperties()
                .incrementVersionId();

        apply(new TechnoTemplateUpdatedEvent(key, template, command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onDeleteTechnoTemplateCommand(DeleteTechnoTemplateCommand command) {
        //TODO Est-ce qu'on incrémente le versionId ? => Etudier la re-création d'une techno ?
        // si le template n'existe pas, cette commande n'a pas d'effet de bord
        if (this.templates.containsKey(command.getTemplateName())) {
            apply(new TechnoTemplateDeletedEvent(key, command.getTemplateName(), command.getUser()));
        }
    }

    /*** EVENT HANDLERS ***/

    //TODO Logs plus précis (avec données ?)
    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void onTechnoCreatedEvent(TechnoCreatedEvent event) {
        this.key = event.getTechno().getKey();
        log.debug("Techno created (aggregate is live ? {})", isLive());
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void onTechnoDeletedEvent(TechnoDeletedEvent event) {
        log.debug("Techno deleted (aggregate is live ? {})", isLive());
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void onTemplateAddedToTechnoEvent(TemplateAddedToTechnoEvent event) {
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
        log.debug("Template ajouté à la techno (aggregate is live ? {})", isLive());
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void onTechnoTemplateUpdatedEvent(TechnoTemplateUpdatedEvent event) {
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
        log.debug("Template mis à jour. ");
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void onTechnoTemplateDeletedEvent(TechnoTemplateDeletedEvent event) {
        this.templates.remove(event.getTemplateName());
        log.debug("Template supprimé. ");
    }
}
