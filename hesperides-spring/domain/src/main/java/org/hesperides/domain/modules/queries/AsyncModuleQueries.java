package org.hesperides.domain.modules.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * lance les query via Axon.
 */
@Component
public class AsyncModuleQueries implements ModulesQueries {

    private final QueryGateway queryGateway;

    public AsyncModuleQueries(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    /**
     * trouvé ici: https://stackoverflow.com/questions/5207163/how-to-do-myclassstring-class-in-java
     *
     * @param tClass
     * @param <T2>
     * @return
     */
    @SuppressWarnings("unchecked")
    static public <T2> Class<List<T2>> listOf(Class<T2> tClass) {
        return (Class<List<T2>>) (Class<?>) (List.class);
    }

    @SuppressWarnings("unchecked")
    static public <T2> Class<Optional<T2>> optionalOf(Class<T2> tClass) {
        return (Class<Optional<T2>>) (Class<?>) (Optional.class);
    }

    @Override
    public Optional<ModuleView> query(ModuleByIdQuery query) {
        try {
            return queryGateway.send(query, optionalOf(ModuleView.class)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> queryAllModuleNames(ModulesNamesQuery query) {
        try {
            return queryGateway.send(new ModulesNamesQuery(), listOf(String.class)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<TemplateView> queryTemplateByName(TemplateByNameQuery query) {
        try {
            return queryGateway.send(query,optionalOf(TemplateView.class)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
