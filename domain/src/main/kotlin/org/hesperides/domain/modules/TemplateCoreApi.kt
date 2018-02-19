package org.hesperides.domain.modules

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.domain.modules.entities.Module
import org.hesperides.domain.modules.entities.Template

data class CreateTemplateCommand(@TargetAggregateIdentifier val moduleKey: Module.Key, val template: Template)
data class UpdateTemplateCommand(@TargetAggregateIdentifier val moduleKey: Module.Key, val template: Template)
data class DeleteTemplateCommand(@TargetAggregateIdentifier val moduleKey: Module.Key, val templateName: String)

data class TemplateCreatedEvent(val moduleKey: Module.Key, val template: Template)
data class TemplateDeletedEvent(val moduleKey: Module.Key, val templateName: String)
data class TemplateUpdatedEvent(val moduleKey: Module.Key, val template: Template)

data class TemplateByNameQuery(val moduleKey: Module.Key, val templateName: String)
