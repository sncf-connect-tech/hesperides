package org.hesperides.core.domain.authorizations

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.core.domain.security.UserEvent
import org.hesperides.core.domain.security.entities.ApplicationAuthorities
import org.hesperides.core.domain.security.entities.User

// Command

data class CreateApplicationAuthoritiesCommand(val applicationAuthorities: ApplicationAuthorities, val user: User)
data class UpdateApplicationAuthoritiesCommand(@TargetAggregateIdentifier val id: String, val applicationAuthorities: ApplicationAuthorities, val user: User)

// Event

data class ApplicationAuthoritiesCreatedEvent(val id: String, val applicationAuthorities: ApplicationAuthorities, override val user: String) : UserEvent(user)
data class ApplicationAuthoritiesUpdatedEvent(val id: String, val applicationAuthorities: ApplicationAuthorities, override val user: String) : UserEvent(user)

// Queries

data class GetApplicationAuthoritiesQuery(val applicationName: String)