package org.hesperides.domain.platforms

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.domain.platforms.entities.Platform
import org.hesperides.domain.security.User
import org.hesperides.domain.security.UserEvent

// Command
data class CreatePlatformCommand(val platform: Platform, val user: User)
data class DeletePlatformCommand(@TargetAggregateIdentifier val platformKey: Platform.Key, val user: User)

// Event
data class PlatformCreatedEvent(val platform: Platform, override val user: User) : UserEvent(user)
data class PlatformDeletedEvent(val platformKey: Platform.Key, override val user: User) : UserEvent(user)

// Query
data class GetPlatformByKeyQuery(val platformKey: Platform.Key)