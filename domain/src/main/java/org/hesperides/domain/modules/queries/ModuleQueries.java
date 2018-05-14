package org.hesperides.domain.modules.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.domain.framework.Queries;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Permet de regrouper les queries
 */
@Component
public class ModuleQueries extends Queries {

    protected ModuleQueries(QueryGateway queryGateway) {
        super(queryGateway);
    }

    public boolean moduleExists(Module.Key moduleKey) {
        return querySync(new ModuleAlreadyExistsQuery(moduleKey), Boolean.class);
    }

    public Optional<ModuleView> getModule(Module.Key moduleKey) {
        return querySyncOptional(new GetModuleByKeyQuery(moduleKey), ModuleView.class);
    }

    public List<String> getModulesNames() {
        return querySyncList(new GetModulesNamesQuery(), String.class);
    }

    public List<String> getModuleVersions(String moduleName) {
        return querySyncList(new GetModuleVersionsQuery(moduleName), String.class);
    }

    public List<String> getModuleTypes(String moduleName, String moduleVersion) {
        return querySyncList(new GetModuleVersionTypesQuery(moduleName, moduleVersion), String.class);
    }

    public Optional<TemplateView> getTemplate(Module.Key moduleKey, String templateName) {
        return querySyncOptional(new GetTemplateByNameQuery(moduleKey, templateName), TemplateView.class);
    }

    public List<ModuleView> search(String query) {
        return querySyncList(new SearchModulesQuery(query), ModuleView.class);
    }

    public List<TemplateView> getTemplates(TemplateContainer.Key moduleKey) {
        return querySyncList(new GetModuleTemplatesQuery(moduleKey), TemplateView.class);
    }
}
