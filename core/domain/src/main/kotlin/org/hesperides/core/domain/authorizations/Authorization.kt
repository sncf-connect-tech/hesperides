package org.hesperides.core.domain.authorizations

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.core.domain.security.UserEvent
import org.hesperides.core.domain.security.entities.ApplicationDirectoryGroups
import org.hesperides.core.domain.security.entities.User

// Command

data class CreateApplicationDirectoryGroupsCommand(val applicationDirectoryGroups: ApplicationDirectoryGroups, val user: User)
data class UpdateApplicationDirectoryGroupsCommand(@TargetAggregateIdentifier val id: String, val applicationDirectoryGroups: ApplicationDirectoryGroups, val user: User)

// Event

data class ApplicationDirectoryGroupsCreatedEvent(val id: String, val applicationDirectoryGroups: ApplicationDirectoryGroups, override val user: String) : UserEvent(user)
data class ApplicationDirectoryGroupsUpdatedEvent(val id: String, val applicationDirectoryGroups: ApplicationDirectoryGroups, override val user: String) : UserEvent(user)

// Queries

data class GetApplicationDirectoryGroupsQuery(val applicationName: String)