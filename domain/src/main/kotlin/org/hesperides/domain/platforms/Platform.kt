package org.hesperides.domain.platforms

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.domain.platforms.entities.Platform
import org.hesperides.domain.security.User
import org.hesperides.domain.security.UserEvent
import org.hesperides.domain.templatecontainers.entities.TemplateContainer

// Command
data class CreatePlatformCommand(val platform: Platform, val user: User)
data class DeletePlatformCommand(@TargetAggregateIdentifier val platformKey: Platform.Key, val user: User)
data class UpdatePlatformCommand(@TargetAggregateIdentifier val key: Platform.Key, val newDefintion: Platform, val copyProps: Boolean, val user: User)

// Event
data class PlatformCreatedEvent(val platform: Platform, override val user: User) : UserEvent(user)
data class PlatformDeletedEvent(val platformKey: Platform.Key, override val user: User) : UserEvent(user)
data class PlatformUpdatedEvent(val key: Platform.Key, val newDefinition: Platform, override val user: User) : UserEvent(user)

// Query
data class GetPlatformByKeyQuery(val platformKey: Platform.Key)

data class GetPlatformsUsingModuleQuery(val moduleKey: TemplateContainer.Key)

data class GetApplicationByNameQuery(val applicationName: String)

data class SearchApplicationsByNameQuery(val input: String)