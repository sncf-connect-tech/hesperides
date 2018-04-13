package org.hesperides.domain.modules

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.domain.templatecontainer.entities.Template
import org.hesperides.domain.security.User
import org.hesperides.domain.security.UserEvent
import org.hesperides.domain.templatecontainer.entities.TemplateContainer

// Command
data class CreateTemplateCommand(@TargetAggregateIdentifier val moduleKey: TemplateContainer.Key, val template: Template, val user: User)
data class UpdateTemplateCommand(@TargetAggregateIdentifier val moduleKey: TemplateContainer.Key, val template: Template, val user: User)
data class DeleteTemplateCommand(@TargetAggregateIdentifier val moduleKey: TemplateContainer.Key, val templateName: String, val user: User)

// Event
data class TemplateCreatedEvent(val moduleKey: TemplateContainer.Key, val template: Template, override val user: User) : UserEvent(user)
data class TemplateDeletedEvent(val moduleKey: TemplateContainer.Key, val templateName: String, override val user: User) : UserEvent(user)
data class TemplateUpdatedEvent(val moduleKey: TemplateContainer.Key, val template: Template, override val user: User) : UserEvent(user)

// Query
data class TemplateByNameQuery(val moduleKey: TemplateContainer.Key, val templateName: String)
