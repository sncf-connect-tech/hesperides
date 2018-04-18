package org.hesperides.domain.technos

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.domain.security.User
import org.hesperides.domain.security.UserEvent
import org.hesperides.domain.technos.entities.Techno
import org.hesperides.domain.templatecontainer.entities.Template
import org.hesperides.domain.templatecontainer.entities.TemplateContainer

// Commands
data class CreateTechnoCommand(val techno: Techno, val user: User)
data class AddTemplateToTechnoCommand(@TargetAggregateIdentifier val technoKey: TemplateContainer.Key, val template: Template, val user: User)

// Events
data class TechnoCreatedEvent(val techno: Techno, override val user: User) : UserEvent(user)
data class TemplateAddedToTechnoEvent(val technoKey: TemplateContainer.Key, val template: Template, override val user: User) : UserEvent(user)

// Queries
data class TechnoAlreadyExistsQuery(val technoKey: TemplateContainer.Key)
data class GetTemplateQuery(val technoKey: TemplateContainer.Key, val templateName: String)
