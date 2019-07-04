package org.hesperides.core.domain.security.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.core.domain.authorizations.UpdateApplicationAuthorities;
import org.hesperides.core.domain.modules.*;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.core.domain.templatecontainers.entities.Template;

import java.io.Serializable;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Slf4j
@NoArgsConstructor
@Aggregate(snapshotTriggerDefinition = "snapshotTrigger")
class AuthorizationAggregate implements Serializable {

    @AggregateIdentifier
    private String id;

    /*** COMMAND HANDLERS ***/

    @CommandHandler
    @SuppressWarnings("unused")
    public void onUpdateApplicationAuthorities(UpdateApplicationAuthorities command) {
        log.debug("onUpdateApplicationAuthorities - moduleId: %s - key: %s - versionId: %s - user: %s",
                command.getModuleId(), command.getModule().getKey().getNamespaceWithoutPrefix(), command.getModule().getVersionId(), command.getUser());
        Module module = command.getModule()
                .validateIsWorkingCopy()
                .validateVersionId(versionId)
                .incrementVersiondId();
        apply(new ModuleTechnosUpdatedEvent(command.getModuleId(), module.getTechnos(), module.getVersionId(), command.getUser().getName()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onDeleteModuleCommand(DeleteModuleCommand command) {
        log.debug("onUpdateModuleTechnosCommand - moduleId: %s - user: %s",
                command.getModuleId(), command.getUser());
        apply(new ModuleDeletedEvent(command.getModuleId(), command.getUser().getName()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onCreateTemplateCommand(CreateTemplateCommand command) {
        log.debug("onCreateTemplateCommand - moduleId: %s - key: %s - templateName: %s - user: %s",
                command.getModuleId(), command.getModuleKey(), command.getTemplate().getName(), command.getUser());
        Template template = command.getTemplate()
                .validateNameNotTaken(templates, key)
                .validateProperties()
                .initializeVersionId();
        apply(new TemplateCreatedEvent(command.getModuleId(), command.getModuleKey(), template, command.getUser().getName()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onUpdateTemplateCommand(UpdateTemplateCommand command) {
        log.debug("onUpdateTemplateCommand - moduleId: %s - key: %s - templateName: %s - user: %s",
                command.getModuleId(), command.getModuleKey(), command.getTemplate().getName(), command.getUser());
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
        log.debug("onDeleteTemplateCommand - moduleId: %s - key: %s - templateName: %s - user: %s",
                command.getModuleId(), command.getModuleKey(), command.getTemplateName(), command.getUser());
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
        log.debug("onModuleCreatedEvent - moduleId: %s - key: %s - versionId: %s - user: %s",
                event.getModuleId(), event.getModule().getKey(), event.getModule().getVersionId(), event.getUser());
        this.id = event.getModuleId();
        this.key = event.getModule().getKey();
        this.versionId = event.getModule().getVersionId();
        event.getModule().getTemplates().forEach(template ->
                this.templates.put(template.getName(), template)
        );
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void onModuleTechnosUpdatedEvent(ModuleTechnosUpdatedEvent event) {
        log.debug("onModuleTechnosUpdatedEvent - moduleId: %s - versionId: %s - user: %s",
                event.getModuleId(), event.getVersionId(), event.getUser());
        this.versionId = event.getVersionId();
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void onModuleDeletedEvent(ModuleDeletedEvent event) {
        log.debug("onModuleDeletedEvent - moduleId: %s - user: %s",
                event.getModuleId(), event.getUser());
    }

    @EventSourcingHandler
    public void onTemplateCreatedEvent(TemplateCreatedEvent event) {
        log.debug("onTemplateCreatedEvent - moduleId: %s - key: %s - templateName: %s - user: %s",
                event.getModuleId(), event.getModuleKey(), event.getTemplate().getName(), event.getUser());
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
    }

    @EventSourcingHandler
    public void onTemplateUpdatedEvent(TemplateUpdatedEvent event) {
        log.debug("onTemplateCreatedEvent - moduleId: %s - key: %s - templateName: %s - user: %s",
                event.getModuleId(), event.getModuleKey(), event.getTemplate().getName(), event.getUser());
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
    }

    @EventSourcingHandler
    public void onTemplateDeletedEvent(TemplateDeletedEvent event) {
        log.debug("onTemplateCreatedEvent - moduleId: %s - key: %s - templateName: %s - user: %s",
                event.getModuleId(), event.getModuleKey(), event.getTemplateName(), event.getUser());
        this.templates.remove(event.getTemplateName());
    }
}
