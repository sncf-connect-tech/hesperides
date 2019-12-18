package org.hesperides.core.domain.platforms

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.core.domain.modules.entities.Module
import org.hesperides.core.domain.platforms.entities.Platform
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty
import org.hesperides.core.domain.security.UserEvent
import org.hesperides.core.domain.security.entities.User

// Command

data class CreatePlatformCommand(val platform: Platform, val user: User)
data class DeletePlatformCommand(@TargetAggregateIdentifier val platformId: String, val platformKey: Platform.Key, val user: User)
data class UpdatePlatformCommand(@TargetAggregateIdentifier val platformId: String, val platform: Platform, val copyPropertiesForUpgradedModules: Boolean, val user: User)
data class UpdatePlatformPropertiesCommand(@TargetAggregateIdentifier val platformId: String, val providedPlatformVersionId: Long, val providedPropertiesVersionId: Long, val expectedPropertiesVersionId: Long, val valuedProperties: List<ValuedProperty>, val user: User)
data class UpdatePlatformModulePropertiesCommand(@TargetAggregateIdentifier val platformId: String, val propertiesPath: String, val providedPlatformVersionId: Long, val providedPropertiesVersionId: Long, val expectedPropertiesVersionId: Long, val valuedProperties: List<AbstractValuedProperty>, val userComment: String, val user: User)
data class RestoreDeletedPlatformCommand(@TargetAggregateIdentifier val platformId: String, val user: User)

// Event

open class PlatformEvent(@Transient open val platformId: String, @Transient override val user: String) : UserEvent(user)
open class PlatformEventWithKey(@Transient open val platformKey: Platform.Key, @Transient override val platformId: String, @Transient override val user: String) : PlatformEvent(platformId, user)

data class PlatformCreatedEvent(override val platformId: String, val platform: Platform, override val user: String) : PlatformEventWithKey(platform.key, platformId, user)
data class PlatformUpdatedEvent(override val platformId: String, val platform: Platform, val copyPropertiesForUpgradedModules: Boolean, override val user: String) : PlatformEventWithKey(platform.key, platformId, user)
data class PlatformDeletedEvent(override val platformId: String, override val platformKey: Platform.Key, override val user: String) : PlatformEventWithKey(platformKey, platformId, user)
data class PlatformPropertiesUpdatedEvent(override val platformId: String, val platformVersionId: Long, val globalPropertiesVersionId: Long, val valuedProperties: List<ValuedProperty>, override val user: String) : PlatformEvent(platformId, user)
data class PlatformModulePropertiesUpdatedEvent(override val platformId: String, val propertiesPath: String, val platformVersionId: Long, val propertiesVersionId: Long, val valuedProperties: List<AbstractValuedProperty>, val userComment: String, override val user: String) : PlatformEvent(platformId, user)
data class RestoreDeletedPlatformEvent(override val platformId: String, override val user: String) : PlatformEvent(platformId, user)

// Query

data class GetPlatformIdFromKeyQuery(val platformKey: Platform.Key)
data class GetPlatformIdFromEvents(val platformKey: Platform.Key)
data class GetPlatformByIdQuery(val platformId: String)
data class GetPlatformByKeyQuery(val platformKey: Platform.Key)
data class GetPlatformAtPointInTimeQuery(val platformId: String, val timestamp: Long)
data class PlatformExistsByKeyQuery(val platformKey: Platform.Key)
data class GetApplicationByNameQuery(val applicationName: String, val hidePlatformsModules: Boolean)
data class GetPlatformsUsingModuleQuery(val moduleKey: Module.Key)
class GetApplicationNamesQuery
data class SearchApplicationsQuery(val applicationName: String)
data class SearchPlatformsQuery(val applicationName: String, val platformName: String? = null)
data class GetPropertiesVersionIdQuery(val platformId: String, val propertiesPath: String, val timestamp: Long) // timestamp == -1 => no timestamp
data class GetGlobalPropertiesVersionIdQuery(val platformKey: Platform.Key)
data class GetGlobalPropertiesQuery(val platformKey: Platform.Key)
data class GetInstancesModelQuery(val platformKey: Platform.Key, val propertiesPath: String)
data class InstanceExistsQuery(val platformKey: Platform.Key, val propertiesPath: String, val instanceName: String)
data class ApplicationExistsQuery(val applicationName: String)
class GetAllApplicationsDetailQuery
