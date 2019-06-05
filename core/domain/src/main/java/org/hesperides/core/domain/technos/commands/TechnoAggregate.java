package org.hesperides.core.domain.technos.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateMember;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.core.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.core.domain.technos.*;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;
import static org.axonframework.commandhandling.model.AggregateLifecycle.isLive;

@Slf4j
@NoArgsConstructor
@Aggregate(snapshotTriggerDefinition = "snapshotTrigger")
class TechnoAggregate implements Serializable {

    @AggregateIdentifier
    private String id;
    private TemplateContainer.Key key;
    @AggregateMember
    private Map<String, Template> templates = new HashMap<>();

    /*** COMMAND HANDLERS ***/

    @CommandHandler
    @SuppressWarnings("unused")
    public TechnoAggregate(CreateTechnoCommand command) {
        log.debug("Applying CreateTechnoCommand...");
        command.getTechno().validateTemplates();
        apply(new TechnoCreatedEvent(UUID.randomUUID().toString(), command.getTechno(), command.getUser().getName()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onDeleteTechnoCommand(DeleteTechnoCommand command) {
        log.debug("Applying delete techno command...");
        apply(new TechnoDeletedEvent(command.getTechnoId(), command.getUser().getName()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onAddTemplateToTechnoCommand(AddTemplateToTechnoCommand command) {
        log.debug("Applying AddTemplateToTechnoCommand...");

        Template template = command.getTemplate()
                .validateNameNotTaken(templates, key)
                .validateProperties()
                .initializeVersionId();

        apply(new TemplateAddedToTechnoEvent(command.getTechnoId(), template, command.getUser().getName()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onUpdateTechnoTemplateCommand(UpdateTechnoTemplateCommand command) {
        log.debug("Applying update template command...");

        Template template = command.getTemplate()
                .validateExistingName(templates, key)
                .validateVersionId(getExpectedVersionId(command))
                .validateProperties()
                .incrementVersionId();

        apply(new TechnoTemplateUpdatedEvent(command.getTechnoId(), template, command.getUser().getName()));
    }

    private Long getExpectedVersionId(UpdateTechnoTemplateCommand command) {
        return templates.get(command.getTemplate().getName()).getVersionId();
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onDeleteTechnoTemplateCommand(DeleteTechnoTemplateCommand command) {
        if (!this.templates.containsKey(command.getTemplateName())) {
            throw new TemplateNotFoundException(key, command.getTemplateName());
        }
        apply(new TechnoTemplateDeletedEvent(command.getTechnoId(), command.getTemplateName(), command.getUser().getName()));
    }

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void onTechnoCreatedEvent(TechnoCreatedEvent event) {
        this.id = event.getTechnoId();
        this.key = event.getTechno().getKey();
        event.getTechno().getTemplates().forEach(template -> templates.put(template.getName(), template));
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

