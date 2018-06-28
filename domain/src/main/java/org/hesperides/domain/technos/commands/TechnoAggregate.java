package org.hesperides.domain.technos.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateMember;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.domain.modules.exceptions.DuplicateTemplateCreationException;
import org.hesperides.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.domain.technos.*;
import org.hesperides.domain.templatecontainers.entities.AbstractProperty;
import org.hesperides.domain.templatecontainers.entities.Template;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
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

        // Vérifie qu'on a pas déjà un template avec ce nom
        if (this.templates.containsKey(command.getTemplate().getName())) {
            throw new DuplicateTemplateCreationException(command.getTemplate());
        }

        // Vérifie les propriétés
        List<AbstractProperty> abstractProperties = AbstractProperty.extractPropertiesFromTemplate(command.getTemplate());
        AbstractProperty.validateProperties(abstractProperties);

        // Initialise le version_id du template à 1
        Template template = command.getTemplate().initVersionId();

        apply(new TemplateAddedToTechnoEvent(command.getTechnoKey(), template, command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onUpdateTechnoTemplateCommand(UpdateTechnoTemplateCommand command) {
        log.debug("Applying update template command...");

        // Vérifie qu'on a déjà un template avec ce nom
        if (!templates.containsKey(command.getTemplate().getName())) {
            throw new TemplateNotFoundException(key, command.getTemplate().getName());
        }

        // Vérifie que le template n'a été modifié entre temps
        Long expectedVersionId = templates.get(command.getTemplate().getName()).getVersionId();
        command.getTemplate().validateVersionId(expectedVersionId);

        // Vérifie les propriétés
        List<AbstractProperty> abstractProperties = AbstractProperty.extractPropertiesFromTemplate(command.getTemplate());
        AbstractProperty.validateProperties(abstractProperties);

        // Met à jour le version_id
        Template template = command.getTemplate().incrementVersionId();

        apply(new TechnoTemplateUpdatedEvent(key, template, command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onDeleteTechnoTemplateCommand(DeleteTechnoTemplateCommand command) {
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
    public void onTechnoDeletedEvent(TechnoDeletedEvent event) { //TODO Pourquoi public ? Est-ce que ça fonctionne ?
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
