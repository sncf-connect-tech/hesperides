package org.hesperides.domain.technos

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.domain.security.User
import org.hesperides.domain.security.UserEvent
import org.hesperides.domain.technos.entities.Techno
import org.hesperides.domain.templatecontainers.entities.AbstractProperty
import org.hesperides.domain.templatecontainers.entities.Template
import org.hesperides.domain.templatecontainers.entities.TemplateContainer

// Commands
data class CreateTechnoCommand(val techno: Techno, val user: User)
data class DeleteTechnoCommand(@TargetAggregateIdentifier val technoKey: TemplateContainer.Key, val user: User)
data class AddTemplateToTechnoCommand(@TargetAggregateIdentifier val technoKey: TemplateContainer.Key, val template: Template, val user: User)
data class UpdateTechnoTemplateCommand(@TargetAggregateIdentifier val technoKey: TemplateContainer.Key, val template: Template, val user: User)
data class DeleteTechnoTemplateCommand(@TargetAggregateIdentifier val technoKey: TemplateContainer.Key, val templateName: String, val user: User)

// Events
data class TechnoCreatedEvent(val techno: Techno, override val user: User) : UserEvent(user)
data class TechnoDeletedEvent(val technoKey: TemplateContainer.Key, override val user: User) : UserEvent(user)
data class TemplateAddedToTechnoEvent(val technoKey: TemplateContainer.Key, val template: Template, override val user: User) : UserEvent(user)
data class TechnoTemplateUpdatedEvent(val technoKey: TemplateContainer.Key, val template: Template, override val user: User) : UserEvent(user)
data class TechnoTemplateDeletedEvent(val technoKey: TemplateContainer.Key, val templateName: String, override val user: User) : UserEvent(user)

// Queries
data class TechnoAlreadyExistsQuery(val technoKey: TemplateContainer.Key)
data class GetTemplateQuery(val technoKey: TemplateContainer.Key, val templateName: String)
data class GetTemplatesQuery(val technoKey: TemplateContainer.Key)
data class GetTechnoQuery(val technoKey: TemplateContainer.Key)
data class SearchTechnosQuery(val input: String)
data class GetTechnoPropertiesQuery(val technoKey: TemplateContainer.Key)
