package org.hesperides.domain.modules.queries;

import org.axonframework.queryhandling.QueryExecutionException;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.responsetypes.ResponseTypes;
import org.hesperides.domain.framework.OptionalResponseType;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Permet de regrouper les queries
 */
@Component
public class ModuleQueries {

    private final QueryGateway queryGateway;

    public ModuleQueries(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    private <R> R querySync(Object query, Class<R> responseType) {
        try {
            return queryGateway.query(query, responseType).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new QueryExecutionException(e.getMessage(), e);
        }
    }

    private <R> Optional<R> querySyncOptional(Object query, Class<R> responseType) {
        try {
            return queryGateway.query(query, OptionalResponseType.optionalInstancesOf(responseType)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new QueryExecutionException(e.getMessage(), e);
        }
    }

    private <R> List<R> querySyncList(Object query, Class<R> responseType) {
        try {
            return queryGateway.query(query, ResponseTypes.multipleInstancesOf(responseType)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new QueryExecutionException(e.getMessage(), e);
        }
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
