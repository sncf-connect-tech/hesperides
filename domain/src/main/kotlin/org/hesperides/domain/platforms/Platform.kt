package org.hesperides.domain.platforms

import org.hesperides.domain.platforms.entities.Platform
import org.hesperides.domain.security.User
import org.hesperides.domain.security.UserEvent

// Command
data class CreatePlatformCommand(val platform: Platform, val user: User)

// Event
data class PlatformCreatedEvent(val platform: Platform, override val user: User) : UserEvent(user)

// Query
data class GetPlatformByKeyQuery(val platformKey: Platform.Key)