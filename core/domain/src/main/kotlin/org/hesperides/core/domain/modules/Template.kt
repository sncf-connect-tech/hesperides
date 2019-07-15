package org.hesperides.core.domain.modules

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.core.domain.security.UserEvent
import org.hesperides.core.domain.security.entities.User
import org.hesperides.core.domain.templatecontainers.entities.Template
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer

// Dans les commandes / événements ci-dessous, le champ moduleKey est uniquement utile
// pour remonter des messages d'erreurs contenant la clef de module

// Command

data class CreateTemplateCommand(@TargetAggregateIdentifier val moduleId: String, val moduleKey: TemplateContainer.Key, val template: Template, val user: User)
data class UpdateTemplateCommand(@TargetAggregateIdentifier val moduleId: String, val moduleKey: TemplateContainer.Key, val template: Template, val user: User)
data class DeleteTemplateCommand(@TargetAggregateIdentifier val moduleId: String, val moduleKey: TemplateContainer.Key, val templateName: String, val user: User)

// Event

data class TemplateCreatedEvent(val moduleId: String, val moduleKey: TemplateContainer.Key, val template: Template, override val user: String) : UserEvent(user)
data class TemplateUpdatedEvent(val moduleId: String, val moduleKey: TemplateContainer.Key, val template: Template, override val user: String) : UserEvent(user)
data class TemplateDeletedEvent(val moduleId: String, val moduleKey: TemplateContainer.Key, val templateName: String, override val user: String) : UserEvent(user)

// Query

data class GetTemplateByNameQuery(val moduleKey: TemplateContainer.Key, val templateName: String)
data class GetModuleTemplatesQuery(val moduleKey: TemplateContainer.Key)
