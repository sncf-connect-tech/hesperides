package org.hesperides.domain.modules.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.domain.framework.Queries;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
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

    public boolean moduleExist(Module.Key newModuleKey) {
        return querySync(new ModuleAlreadyExistsQuery(newModuleKey), Boolean.class);
    }

    public Optional<ModuleView> getModule(Module.Key moduleKey) {
        return querySyncOptional(new ModuleByIdQuery(moduleKey), ModuleView.class);
    }

    public List<String> getModulesNames() {
        return querySyncList(new ModulesNamesQuery(), String.class);
    }

    public List<String> getModuleVersions(String moduleName) {
        return querySyncList(new ModuleVersionsQuery(moduleName), String.class);
    }

    public List<String> getModuleTypes(String moduleName, String moduleVersion) {
        return querySyncList(new ModuleTypesQuery(moduleName, moduleVersion), String.class);
    }

    public Optional<TemplateView> getTemplate(Module.Key moduleKey, String templateName) {
        return querySyncOptional(new TemplateByNameQuery(moduleKey, templateName), TemplateView.class);
    }
}
