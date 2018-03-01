package org.hesperides.domain.modules

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.domain.modules.entities.Module

// Command
data class CreateModuleCommand(val module: Module)
data class UpdateModuleCommand(val module: Module)

// Event
data class ModuleCopiedEvent(val moduleKey: Module.Key, val sourceModuleKey: Module.Key)
data class ModuleCreatedEvent(val module: Module)
data class ModuleUpdatedEvent(val module: Module)

// Query
data class ModuleAlreadyExistsQuery(val moduleKey: Module.Key)
data class ModuleByIdQuery(val moduleKey: Module.Key)
class ModulesNamesQuery
data class ModuleTypesQuery(val moduleName: String, val moduleVersion: String)
data class ModuleVersionsQuery(val moduleName: String)
