package org.hesperides.domain.modules

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.domain.modules.entities.Module

data class CreateModuleCommand(@TargetAggregateIdentifier val moduleKey: Module.Key)

data class ModuleCopiedEvent(val moduleKey: Module.Key, val sourceModuleKey: Module.Key)
data class ModuleCreatedEvent(val moduleKey: Module.Key)

data class ModuleAlreadyExistsQuery(val moduleKey: Module.Key)
data class ModuleByIdQuery(val moduleKey: Module.Key)
class ModulesNamesQuery()
