package org.hesperides.domain.technos.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateMember;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.domain.modules.exceptions.DuplicateTemplateCreationException;
import org.hesperides.domain.technos.AddTemplateToTechnoCommand;
import org.hesperides.domain.technos.CreateTechnoCommand;
import org.hesperides.domain.technos.TechnoCreatedEvent;
import org.hesperides.domain.technos.TemplateAddedToTechnoEvent;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainer.entities.Template;

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
    private Techno.Key key;
    @AggregateMember
    private Map<String, Template> templates = new HashMap<>();

    @CommandHandler
    public TechnoAggregate(CreateTechnoCommand command) {
        log.debug("Applying CreateTechnoCommand...");
        apply(new TechnoCreatedEvent(command.getTechno(), command.getUser()));
    }

    @CommandHandler
    public void addTemplate(AddTemplateToTechnoCommand command) {
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

        apply(new TemplateAddedToTechnoEvent(command.getTechnoKey(), newTemplate, command.getUser()));
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void on(TechnoCreatedEvent event) {
        this.key = event.getTechno().getKey();
        log.debug("Techno created (aggregate is live ? {})", isLive());
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void on(TemplateAddedToTechnoEvent event) {
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
        log.debug("Template added to techno (aggregate is live ? {})", isLive());
    }
}
