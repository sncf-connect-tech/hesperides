package org.hesperides.core.domain.modules.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.commons.axon.AxonQueries;
import org.hesperides.core.domain.modules.*;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateContainerKeyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ModuleQueries extends AxonQueries {

    protected ModuleQueries(QueryGateway queryGateway) {
        super(queryGateway);
    }

    public Optional<String> getOptionalModuleId(TemplateContainer.Key moduleKey) {
        return querySyncOptional(new GetModuleIdFromKeyQuery(moduleKey), String.class);
    }

    public Optional<ModuleView> getOptionalModule(String moduleId) {
        return querySyncOptional(new GetModuleByIdQuery(moduleId), ModuleView.class);
    }

    public Optional<ModuleView> getOptionalModule(TemplateContainer.Key moduleKey) {
        return querySyncOptional(new GetModuleByKeyQuery(moduleKey), ModuleView.class);
    }

    public boolean moduleExists(TemplateContainer.Key moduleKey) {
        return querySync(new ModuleExistsQuery(moduleKey), Boolean.class);
    }

    public List<String> getModulesName() {
        return querySyncList(new GetModulesNameQuery(), String.class);
    }

    public List<String> getModuleVersions(String moduleName) {
        return querySyncList(new GetModuleVersionsQuery(moduleName), String.class);
    }

    public List<String> getModuleTypes(String moduleName, String moduleVersion) {
        return querySyncList(new GetModuleVersionTypesQuery(moduleName, moduleVersion), String.class);
    }

    public Optional<TemplateView> getTemplate(TemplateContainer.Key moduleKey, String templateName) {
        return querySyncOptional(new GetTemplateByNameQuery(moduleKey, templateName), TemplateView.class);
    }

    public List<ModuleView> search(String query, Integer size) {
        return querySyncList(new SearchModulesQuery(query, size), ModuleView.class);
    }

    public List<TemplateView> getTemplates(TemplateContainer.Key moduleKey) {
        return querySyncList(new GetModuleTemplatesQuery(moduleKey), TemplateView.class);
    }

    public List<AbstractPropertyView> getPropertiesModel(TemplateContainer.Key moduleKey) {
        return querySyncList(new GetModulePropertiesQuery(moduleKey), AbstractPropertyView.class);
    }

    public List<ModuleSimplePropertiesView> getModulesSimpleProperties(List<TemplateContainer.Key> modulesKeys) {
        return querySyncList(new GetModulesSimplePropertiesQuery(modulesKeys), ModuleSimplePropertiesView.class);
    }

    public List<TemplateContainerKeyView> getModulesUsingTechno(String technoId) {
        return querySyncList(new GetModulesUsingTechnoQuery(technoId), TemplateContainerKeyView.class);
    }

    public List<ModuleView> getModulesWithin(List<Module.Key> moduleKeys) {
        return querySyncList(new GetModulesWithinQuery(moduleKeys), ModuleView.class);
    }

    public List<Module.Key> getModulesWithPasswordWithin(List<Module.Key> moduleKeys) {
        return querySyncList(new GetModulesWithPasswordWithinQuery(moduleKeys), Module.Key.class);
    }
}
