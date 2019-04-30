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
import java.util.UUID;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;
import static org.axonframework.commandhandling.model.AggregateLifecycle.isLive;

@Slf4j
@NoArgsConstructor
@Aggregate(snapshotTriggerDefinition = "snapshotTrigger")
class ModuleAggregate implements Serializable {

    @AggregateIdentifier
    private String id;
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
        module.getTemplates().forEach(Template::validateProperties);
        apply(new ModuleCreatedEvent(UUID.randomUUID().toString(), module, command.getUser().getName()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onUpdateModuleTechnosCommand(UpdateModuleTechnosCommand command) {
        log.debug("Applying update module command...");

        Module module = command.getModule()
                .validateIsWorkingCopy()
                .validateVersionId(versionId)
                .incrementVersiondId();

        apply(new ModuleTechnosUpdatedEvent(command.getModuleId(), module.getTechnos(), module.getVersionId(), command.getUser().getName()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onDeleteModuleCommand(DeleteModuleCommand command) {
        log.debug("Applying delete module command...");
        apply(new ModuleDeletedEvent(command.getModuleId(), command.getUser().getName()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onCreateTemplateCommand(CreateTemplateCommand command) {
        log.debug("Applying create template command...");

        Template template = command.getTemplate()
                .validateNameNotTaken(templates, key)
                .validateProperties()
                .initializeVersionId();

        apply(new TemplateCreatedEvent(command.getModuleId(), command.getModuleKey(), template, command.getUser().getName()));
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

        apply(new TemplateUpdatedEvent(command.getModuleId(), command.getModuleKey(), template, command.getUser().getName()));
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
        apply(new TemplateDeletedEvent(command.getModuleId(), command.getModuleKey(), command.getTemplateName(), command.getUser().getName()));
    }

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void onModuleCreatedEvent(ModuleCreatedEvent event) {
        this.id = event.getModuleId();
        this.key = event.getModule().getKey();
        this.versionId = event.getModule().getVersionId();
        event.getModule().getTemplates().forEach(template ->
            this.templates.put(template.getName(), template)
        );
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
