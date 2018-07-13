package org.hesperides.core.domain.modules

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.core.domain.modules.entities.Module
import org.hesperides.core.domain.security.User
import org.hesperides.core.domain.security.UserEvent
import org.hesperides.core.domain.technos.entities.Techno
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer

// Command
data class CreateModuleCommand(val module: Module, val user: User)
data class UpdateModuleTechnosCommand(@TargetAggregateIdentifier val moduleKey: TemplateContainer.Key, val module: Module, val user: User)
data class DeleteModuleCommand(@TargetAggregateIdentifier val moduleKey: TemplateContainer.Key, val user: User)

// Event
data class ModuleCreatedEvent(val module: Module, override val user: User) : UserEvent(user)
data class ModuleTechnosUpdatedEvent(val moduleKey: TemplateContainer.Key, val technos: List<Techno>, val versionId: Long, override val user: User) : UserEvent(user)
data class ModuleDeletedEvent(val moduleKey: TemplateContainer.Key, override val user: User) : UserEvent(user)

// Query
data class ModuleAlreadyExistsQuery(val moduleKey: TemplateContainer.Key)
data class GetModuleByKeyQuery(val moduleKey: TemplateContainer.Key)
class GetModulesNamesQuery
data class GetModuleVersionTypesQuery(val moduleName: String, val moduleVersion: String)
data class GetModuleVersionsQuery(val moduleName: String)
data class SearchModulesQuery(val input: String)
data class GetModulePropertiesQuery(val moduleKey: TemplateContainer.Key)