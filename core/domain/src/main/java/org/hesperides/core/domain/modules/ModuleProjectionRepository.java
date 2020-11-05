package org.hesperides.core.domain.modules;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.queries.ModulePasswordProperties;
import org.hesperides.core.domain.modules.queries.ModulePropertiesView;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateContainerKeyView;

import java.util.List;
import java.util.Optional;

public interface ModuleProjectionRepository {

    /*** EVENT HANDLERS ***/

    @EventHandler
    void onModuleCreatedEvent(ModuleCreatedEvent event);

    @EventHandler
    void onModuleTechnosUpdatedEvent(ModuleTechnosUpdatedEvent event);

    @EventHandler
    void onModuleDeletedEvent(ModuleDeletedEvent event);

    /*** QUERY HANDLERS ***/

    @QueryHandler
    Optional<String> onGetModuleIdFromKeyQuery(GetModuleIdFromKeyQuery query);

    @QueryHandler
    Optional<ModuleView> onGetModuleByIdQuery(GetModuleByIdQuery query);

    @QueryHandler
    Optional<ModuleView> onGetModuleByKeyQuery(GetModuleByKeyQuery query);

    @QueryHandler
    Boolean onModuleExistsQuery(ModuleExistsQuery query);

    @QueryHandler
    List<String> onGetModulesNameQuery(GetModulesNameQuery query);

    @QueryHandler
    List<String> onGetModuleVersionTypesQuery(GetModuleVersionTypesQuery query);

    @QueryHandler
    List<String> onGetModuleVersionsQuery(GetModuleVersionsQuery query);

    @QueryHandler
    List<ModuleView> onSearchModulesQuery(SearchModulesQuery query);

    @QueryHandler
    List<AbstractPropertyView> onGetModulePropertiesQuery(GetModulePropertiesQuery query);

    @QueryHandler
    List<ModulePropertiesView> onGetModulesSimplePropertiesQuery(GetModulesPropertiesQuery query);

    @QueryHandler
    List<TemplateContainerKeyView> onGetModulesUsingTechnoQuery(GetModulesUsingTechnoQuery query);

    @QueryHandler
    List<ModuleView> onGetModulesWithinQuery(GetModulesWithinQuery query);

    @QueryHandler
    List<Module.Key> onGetModulesWithPasswordWithinQuery(GetModulesWithPasswordWithinQuery query);

    @QueryHandler
    List<ModulePasswordProperties> onFindAllPasswordProperties(FindAllPasswordPropertiesQuery query);

    @QueryHandler
    List<ModulePasswordProperties> onFindPasswordPropertiesInQuery(FindPasswordPropertiesInQuery query);
}
