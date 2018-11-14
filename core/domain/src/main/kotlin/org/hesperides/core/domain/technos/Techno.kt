package org.hesperides.core.domain.technos

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.core.domain.security.User
import org.hesperides.core.domain.security.UserEvent
import org.hesperides.core.domain.technos.entities.Techno
import org.hesperides.core.domain.templatecontainers.entities.Template
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer

// Commands

data class CreateTechnoCommand(val techno: Techno, val user: User)
data class DeleteTechnoCommand(@TargetAggregateIdentifier val technoId: String, val user: User)
data class AddTemplateToTechnoCommand(@TargetAggregateIdentifier val technoId: String, val template: Template, val user: User)
data class UpdateTechnoTemplateCommand(@TargetAggregateIdentifier val technoId: String, val template: Template, val user: User)
data class DeleteTechnoTemplateCommand(@TargetAggregateIdentifier val technoId: String, val templateName: String, val user: User)

// Events

data class TechnoCreatedEvent(val technoId: String, val techno: Techno, override val user: String) : UserEvent(user)
data class TechnoDeletedEvent(val technoId: String, override val user: String) : UserEvent(user)
data class TemplateAddedToTechnoEvent(val technoId: String, val template: Template, override val user: String) : UserEvent(user)
data class TechnoTemplateUpdatedEvent(val technoId: String, val template: Template, override val user: String) : UserEvent(user)
data class TechnoTemplateDeletedEvent(val technoId: String, val templateName: String, override val user: String) : UserEvent(user)

// Queries

data class GetTechnoIdFromKeyQuery(val technoKey: TemplateContainer.Key)
data class TechnoExistsQuery(val technoKey: TemplateContainer.Key)
data class GetTemplateQuery(val technoKey: TemplateContainer.Key, val templateName: String)
data class GetTemplatesQuery(val technoKey: TemplateContainer.Key)
data class GetTechnoQuery(val technoKey: TemplateContainer.Key)
data class SearchTechnosQuery(val input: String)
data class GetTechnoPropertiesQuery(val technoKey: TemplateContainer.Key)
