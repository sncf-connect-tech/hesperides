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
data class UpdatePlatformPropertiesCommand(@TargetAggregateIdentifier val platformId: String, val providedPlatformVersionId: Long, val providedPropertiesVersionId: Long, val expectedPropertiesVersionId: Long, val valuedProperties: List<ValuedProperty>, val userComment: String, val user: User)
data class UpdatePlatformModulePropertiesCommand(@TargetAggregateIdentifier val platformId: String, val propertiesPath: String, val providedPlatformVersionId: Long, val providedPropertiesVersionId: Long, val expectedPropertiesVersionId: Long, val valuedProperties: List<AbstractValuedProperty>, val userComment: String, val user: User)
data class RestoreDeletedPlatformCommand(@TargetAggregateIdentifier val platformId: String, val user: User)

// Event

data class PlatformCreatedEvent(val platformId: String, val platform: Platform, override val user: String) : UserEvent(user)
data class PlatformUpdatedEvent(val platformId: String, val platform: Platform, val copyPropertiesForUpgradedModules: Boolean, override val user: String) : UserEvent(user)
data class PlatformDeletedEvent(val platformId: String, val platformKey: Platform.Key, override val user: String) : UserEvent(user)
data class PlatformPropertiesUpdatedEvent(val platformId: String, val platformVersionId: Long, val globalPropertiesVersionId: Long, val valuedProperties: List<ValuedProperty>, val userComment: String, override val user: String) : UserEvent(user)
data class PlatformModulePropertiesUpdatedEvent(val platformId: String, val propertiesPath: String, val platformVersionId: Long, val propertiesVersionId: Long, val valuedProperties: List<AbstractValuedProperty>, val userComment: String, override val user: String) : UserEvent(user)
data class RestoreDeletedPlatformEvent(val platformId: String, override val user: String) : UserEvent(user)

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
data class GetGlobalPropertiesQuery(val platformKey: Platform.Key)
data class GetInstancesModelQuery(val platformKey: Platform.Key, val propertiesPath: String)
data class InstanceExistsQuery(val platformKey: Platform.Key, val propertiesPath: String, val instanceName: String)
data class ApplicationExistsQuery(val applicationName: String)
class GetAllApplicationsDetailQuery
data class IsProductionPlatformQuery(val platformId: String)
class FindAllApplicationsPropertiesQuery
