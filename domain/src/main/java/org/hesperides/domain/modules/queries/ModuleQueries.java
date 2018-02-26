package org.hesperides.domain.modules.queries;

import org.axonframework.queryhandling.QueryExecutionException;
import org.axonframework.queryhandling.QueryGateway;
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

    @SuppressWarnings("unchecked")
    static private <T2> Class<Optional<T2>> optionalOf(Class<T2> tClass) {
        return (Class<Optional<T2>>) (Class<?>) (Optional.class);
    }

    /**
     * Trouvé ici: https://stackoverflow.com/questions/5207163/how-to-do-myclassstring-class-in-java
     *
     * @param tClass
     * @param <T2>
     * @return
     */
    @SuppressWarnings("unchecked")
    static private <T2> Class<List<T2>> listOf(Class<T2> tClass) {
        return (Class<List<T2>>) (Class<?>) (List.class);
    }

    private <R> R querySync(Object query, Class<R> responseType) {
        try {
            return queryGateway.send(query, responseType).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new QueryExecutionException(e.getMessage(), e);
        }
    }

    public boolean moduleExist(Module.Key newModuleKey) {
        return querySync(new ModuleAlreadyExistsQuery(newModuleKey), Boolean.class);
    }

    public Optional<ModuleView> getModule(Module.Key moduleKey) {
        return querySync(new ModuleByIdQuery(moduleKey), optionalOf(ModuleView.class));
    }

    public List<String> getModulesNames() {
        return querySync(new ModulesNamesQuery(), listOf(String.class));
    }

    public List<String> getModuleVersions(String moduleName) {
        return querySync(new ModuleVersionsQuery(moduleName), listOf(String.class));
    }

    public Optional<TemplateView> getTemplate(Module.Key moduleKey, String templateName) {
        return querySync(new TemplateByNameQuery(moduleKey, templateName), optionalOf(TemplateView.class));
    }
}
