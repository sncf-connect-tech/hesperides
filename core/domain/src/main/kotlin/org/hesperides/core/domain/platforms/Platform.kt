package org.hesperides.core.domain.platforms

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.core.domain.modules.entities.Module
import org.hesperides.core.domain.platforms.entities.Platform
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty
import org.hesperides.core.domain.security.User
import org.hesperides.core.domain.security.UserEvent

// Command

data class CreatePlatformCommand(val platform: Platform, val user: User)
data class DeletePlatformCommand(@TargetAggregateIdentifier val platformId: String, val user: User)
data class UpdatePlatformCommand(@TargetAggregateIdentifier val platformId: String, val platform: Platform, val copyProperties: Boolean, val user: User)
data class UpdatePlatformPropertiesCommand(@TargetAggregateIdentifier val platformId: String, val platformVersionId: Long, val valuedProperties: List<ValuedProperty>, val user: User)
data class UpdatePlatformModulePropertiesCommand(@TargetAggregateIdentifier val platformId: String, val modulePath: String, val platformVersionId: Long, val valuedProperties: List<AbstractValuedProperty>, val user: User)

// Event

data class PlatformCreatedEvent(val platformId: String, val platform: Platform, override val user: String) : UserEvent(user)
data class PlatformDeletedEvent(val platformId: String, override val user: String) : UserEvent(user)
data class PlatformUpdatedEvent(val platformId: String, val platform: Platform, override val user: String) : UserEvent(user)
data class PlatformPropertiesUpdatedEvent(val platformId: String, val platformVersionId: Long, val valuedProperties: List<ValuedProperty>, override val user: String) : UserEvent(user)
data class PlatformModulePropertiesUpdatedEvent(val platformId: String, val modulePath: String, val platformVersionId: Long, val valuedProperties: List<AbstractValuedProperty>, override val user: String) : UserEvent(user)

// Query

data class GetPlatformIdFromKeyQuery(val platformKey: Platform.Key)
data class GetPlatformByIdQuery(val platformId: String)
data class GetPlatformByKeyQuery(val platformKey: Platform.Key)
data class PlatformExistsByKeyQuery(val platformKey: Platform.Key)
data class GetApplicationByNameQuery(val applicationName: String)
data class GetPlatformsUsingModuleQuery(val moduleKey: Module.Key)
data class SearchPlatformsQuery(val applicationName: String, val platformName: String? = null)
data class SearchApplicationsQuery(val applicationName: String)
data class GetDeployedModulesPropertiesQuery(val platformKey: Platform.Key, val propertiesPath: String)
data class GetGlobalPropertiesQuery(val platformKey: Platform.Key)
data class GetInstanceModelQuery(val platformKey: Platform.Key, val propertiesPath: String)
data class DeployedModuleExistsQuery(val platformKey: Platform.Key, val moduleKey: Module.Key, val modulePath: String)
data class InstanceExistsQuery(val platformKey: Platform.Key, val moduleKey: Module.Key, val modulePath: String, val instanceName: String)