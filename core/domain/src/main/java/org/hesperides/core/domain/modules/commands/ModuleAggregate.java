package org.hesperides.core.domain.modules.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateMember;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.commons.Logs;
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
        Module module = command.getModule()
                .validateTemplates()
                .initializeVersionId();
        String newUuid = UUID.randomUUID().toString();
        log.debug("ModuleAggregate constructor - moduleId: %s - key: %s - versionId: %s - user: %s",
                newUuid, command.getModule().getKey().getNamespaceWithoutPrefix(), command.getModule().getVersionId(), command.getUser());
        logVersionId(command.getModule().getVersionId());
        apply(new ModuleCreatedEvent(newUuid, module, command.getUser().getName()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onUpdateModuleTechnosCommand(UpdateModuleTechnosCommand command) {
        log.debug("onUpdateModuleTechnosCommand - moduleId: %s - key: %s - versionId: %s - user: %s",
                command.getModuleId(), command.getModule().getKey().getNamespaceWithoutPrefix(), command.getModule().getVersionId(), command.getUser());
        logVersionId(command.getModule().getVersionId());
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
        logVersionId();
        apply(new ModuleDeletedEvent(command.getModuleId(), command.getUser().getName()));
    }

    @CommandHandler
    @SuppressWarnings("unused")
    public void onCreateTemplateCommand(CreateTemplateCommand command) {
        log.debug("onCreateTemplateCommand - moduleId: %s - key: %s - templateName: %s - user: %s",
                command.getModuleId(), command.getModuleKey(), command.getTemplate().getName(), command.getUser());
        logVersionId();
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
        logVersionId();
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
        logVersionId();
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
        logVersionId(event.getModule().getVersionId());
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
        logVersionId(event.getVersionId());
        this.versionId = event.getVersionId();
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void onModuleDeletedEvent(ModuleDeletedEvent event) {
        log.debug("onModuleDeletedEvent - moduleId: %s - user: %s",
                event.getModuleId(), event.getUser());
        logVersionId();
    }

    @EventSourcingHandler
    public void onTemplateCreatedEvent(TemplateCreatedEvent event) {
        log.debug("onTemplateCreatedEvent - moduleId: %s - key: %s - templateName: %s - user: %s",
                event.getModuleId(), event.getModuleKey(), event.getTemplate().getName(), event.getUser());
        logVersionId();
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
    }

    @EventSourcingHandler
    public void onTemplateUpdatedEvent(TemplateUpdatedEvent event) {
        log.debug("onTemplateCreatedEvent - moduleId: %s - key: %s - templateName: %s - user: %s",
                event.getModuleId(), event.getModuleKey(), event.getTemplate().getName(), event.getUser());
        logVersionId();
        this.templates.put(event.getTemplate().getName(), event.getTemplate());
    }

    @EventSourcingHandler
    public void onTemplateDeletedEvent(TemplateDeletedEvent event) {
        log.debug("onTemplateCreatedEvent - moduleId: %s - key: %s - templateName: %s - user: %s",
                event.getModuleId(), event.getModuleKey(), event.getTemplateName(), event.getUser());
        logVersionId();
        log.debug("Aggregate versionId: " + versionId);
        this.templates.remove(event.getTemplateName());
    }

    private void logVersionId() {
        logVersionId(null);
    }

    private void logVersionId(Long entityVersionId) {
        Logs.logVersionId(versionId, entityVersionId);
    }
}
