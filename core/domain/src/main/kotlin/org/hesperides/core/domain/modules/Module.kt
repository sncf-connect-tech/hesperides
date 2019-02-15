package org.hesperides.core.domain.modules

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.core.domain.modules.entities.Module
import org.hesperides.core.domain.security.User
import org.hesperides.core.domain.security.UserEvent
import org.hesperides.core.domain.technos.entities.Techno
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer

// Command

data class CreateModuleCommand(val module: Module, val user: User)
data class UpdateModuleTechnosCommand(@TargetAggregateIdentifier val moduleId: String, val module: Module, val user: User)
data class DeleteModuleCommand(@TargetAggregateIdentifier val moduleId: String, val user: User)

// Event

data class ModuleCreatedEvent(val moduleId: String, val module: Module, override val user: String) : UserEvent(user)
data class ModuleTechnosUpdatedEvent(val moduleId: String, val technos: List<Techno>, val versionId: Long, override val user: String) : UserEvent(user)
data class ModuleDeletedEvent(val moduleId: String, override val user: String) : UserEvent(user)

// Query

data class GetModuleIdFromKeyQuery(val moduleKey: TemplateContainer.Key)
data class GetModuleByIdQuery(val moduleId: String)
data class GetModuleByKeyQuery(val moduleKey: TemplateContainer.Key)
data class ModuleExistsQuery(val moduleKey: TemplateContainer.Key)
class GetModulesNameQuery
data class GetModuleVersionsQuery(val moduleName: String)
data class GetModuleVersionTypesQuery(val moduleName: String, val moduleVersion: String)
data class SearchModulesQuery(val input: String)
data class GetModulePropertiesQuery(val moduleKey: TemplateContainer.Key)
data class GetModulesSimplePropertiesQuery(val modulesKeys: List<TemplateContainer.Key>)
data class GetModulesUsingTechnoQuery(val technoId: String)
