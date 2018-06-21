package org.hesperides.domain.technos.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateMember;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.domain.exceptions.OutOfDateVersionException;
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

    //COMMANDS

    @CommandHandler
    @SuppressWarnings("unused")
    public TechnoAggregate(CreateTechnoCommand command) {
        log.debug("Applying CreateTechnoCommand...");
        apply(new TechnoCreatedEvent(command.getTechno(), command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void on(DeleteTechnoCommand command) {
        log.debug("Applying delete techno command...");
        apply(new TechnoDeletedEvent(command.getTechnoKey(), command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void on(AddTemplateToTechnoCommand command) {
        log.debug("Applying AddTemplateToTechnoCommand...");

        // Vérifie qu'on a pas déjà un template avec ce nom
        if (this.templates.containsKey(command.getTemplate().getName())) {
            throw new DuplicateTemplateCreationException(command.getTemplate());
        }

        // Initialise le version_id du template à 1
        Template template = command.getTemplate();
        Template newTemplate = new Template(
                template.getName(),
                template.getFilename(),
                template.getLocation(),
                template.getContent(),
                template.getRights(),
                1L,
                template.getTemplateContainerKey());

        // Vérifie les propriétés
        List<AbstractProperty> abstractProperties = AbstractProperty.extractPropertiesFromTemplate(newTemplate);
        AbstractProperty.validateProperties(abstractProperties);

        apply(new TemplateAddedToTechnoEvent(command.getTechnoKey(), newTemplate, command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void on(UpdateTechnoTemplateCommand command) {
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
                command.getTechnoKey());

        // Vérifie les propriétés
        List<AbstractProperty> abstractProperties = AbstractProperty.extractPropertiesFromTemplate(templateWithUpdatedVersionId);
        AbstractProperty.validateProperties(abstractProperties);

        apply(new TechnoTemplateUpdatedEvent(key, templateWithUpdatedVersionId, command.getUser()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void on(DeleteTechnoTemplateCommand command) {
        // si le template n'existe pas, cette commande n'a pas d'effet de bord
        if (this.templates.containsKey(command.getTemplateName())) {
            apply(new TechnoTemplateDeletedEvent(key, command.getTemplateName(), command.getUser()));
        }
    }

    //EVENTS

    //TODO Logs en anglais et plus précis (avec données ?)

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void on(TechnoCreatedEvent event) {
        this.key = event.getTechno().getKey();
        log.debug("Techno created (aggregate is live ? {})", isLive());
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    private void on(TechnoDeletedEvent event) { //TODO Pourquoi private ?? Est-ce que ça fonctionne ?
        this.key = event.getTechnoKey(); //TODO Pourquoi ?
        log.debug("Techno deleted (aggregate is live ? {})", isLive());
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void on(TemplateAddedToTechnoEvent event) {
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
        log.debug("Template ajouté à la techno (aggregate is live ? {})", isLive());
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    private void on(TechnoTemplateUpdatedEvent event) {
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
        log.debug("Template mis à jour. ");
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    private void on(TechnoTemplateDeletedEvent event) {
        this.templates.remove(event.getTemplateName());
        log.debug("Template supprimé. ");
    }
}
