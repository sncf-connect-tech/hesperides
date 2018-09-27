package org.hesperides.core.domain.platforms

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.core.domain.platforms.entities.Platform
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty
import org.hesperides.core.domain.security.User
import org.hesperides.core.domain.security.UserEvent
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer

// Command
data class CreatePlatformCommand(val platform: Platform, val user: User)
data class DeletePlatformCommand(@TargetAggregateIdentifier val platformKey: Platform.Key, val user: User)
data class UpdatePlatformCommand(@TargetAggregateIdentifier val platformKey: Platform.Key, val platform: Platform, val copyProperties: Boolean, val user: User)
data class UpdatePlatformPropertiesCommand(@TargetAggregateIdentifier val platformKey: Platform.Key, val platformVersionId: Long, val valuedProperties: List<ValuedProperty>, val user: User)
data class UpdatePlatformModulePropertiesCommand(@TargetAggregateIdentifier val platformKey: Platform.Key, val modulePath: String, val platformVersionId: Long, val valuedProperties: List<AbstractValuedProperty>, val user: User)

// Event
data class PlatformCreatedEvent(val platform: Platform, override val user: User) : UserEvent(user)
data class PlatformDeletedEvent(val platformKey: Platform.Key, override val user: User) : UserEvent(user)
data class PlatformUpdatedEvent(val platformKey: Platform.Key, val platform: Platform, override val user: User) : UserEvent(user)
data class PlatformPropertiesUpdatedEvent(val platformKey: Platform.Key, val platformVersionId: Long, val valuedProperties: List<ValuedProperty>, override val user: User): UserEvent(user)
data class PlatformModulePropertiesUpdatedEvent(val platformKey: Platform.Key, val modulePath: String, val platformVersionId: Long, val valuedProperties: List<AbstractValuedProperty>, override val user: User): UserEvent(user)

// Query
data class PlatformExistsByKeyQuery(val platformKey: Platform.Key)
data class GetPlatformByKeyQuery(val platformKey: Platform.Key)
data class GetApplicationByNameQuery(val applicationName: String)
data class GetPlatformsUsingModuleQuery(val moduleKey: TemplateContainer.Key)
data class SearchPlatformsQuery(val applicationName: String, val platformName: String? = null)
data class SearchApplicationsQuery(val applicationName: String)
data class GetDeployedModulesPropertiesQuery(val platformKey: Platform.Key, val path: String, val user: User)
data class GetGlobalPropertiesQuery(val platformKey: Platform.Key, val path: String, val user: User)
data class GetInstanceModelQuery(val platformKey: Platform.Key, val path: String, val user: User)

