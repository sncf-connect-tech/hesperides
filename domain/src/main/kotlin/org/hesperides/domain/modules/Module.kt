package org.hesperides.domain.modules

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.domain.modules.entities.Module
import org.hesperides.domain.security.User
import org.hesperides.domain.security.UserEvent
import org.hesperides.domain.templatecontainer.entities.TemplateContainer

// Command
data class CreateModuleCommand(val module: Module, val user: User)
data class UpdateModuleCommand(@TargetAggregateIdentifier val moduleKey: TemplateContainer.Key, val module: Module, val user: User)
data class DeleteModuleCommand(@TargetAggregateIdentifier val moduleKey: TemplateContainer.Key, val user: User)

// Event
data class ModuleCopiedEvent(val moduleKey: TemplateContainer.Key, val sourceModuleKey: TemplateContainer.Key, override val user: User) : UserEvent(user)
data class ModuleCreatedEvent(val module: Module, override val user: User) : UserEvent(user)
data class ModuleUpdatedEvent(val module: Module, override val user: User) : UserEvent(user)
data class ModuleDeletedEvent(val moduleKey: TemplateContainer.Key, override val user: User) : UserEvent(user)

// Query
data class ModuleAlreadyExistsQuery(val moduleKey: TemplateContainer.Key)
data class GetModuleByKeyQuery(val moduleKey: TemplateContainer.Key)
class GetModulesNamesQuery
data class GetModuleTypesQuery(val moduleName: String, val moduleVersion: String)
data class GetModuleVersionsQuery(val moduleName: String)
data class SearchModulesQuery(val input: String)