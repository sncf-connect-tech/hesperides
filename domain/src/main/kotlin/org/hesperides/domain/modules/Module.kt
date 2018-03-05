package org.hesperides.domain.modules

import org.hesperides.domain.modules.entities.Module
import org.hesperides.domain.security.User
import org.hesperides.domain.security.UserEvent

// Command
data class CreateModuleCommand(val module: Module, val user: User)
data class UpdateModuleCommand(val module: Module, val user: User)

// Event
data class ModuleCopiedEvent(val moduleKey: Module.Key, val sourceModuleKey: Module.Key, override val user: User) : UserEvent(user)
data class ModuleCreatedEvent(val module: Module, override val user: User) : UserEvent(user)
data class ModuleUpdatedEvent(val module: Module, override val user: User) : UserEvent(user)

// Query
data class ModuleAlreadyExistsQuery(val moduleKey: Module.Key)
data class ModuleByIdQuery(val moduleKey: Module.Key)
class ModulesNamesQuery
data class ModuleTypesQuery(val moduleName: String, val moduleVersion: String)
data class ModuleVersionsQuery(val moduleName: String)
