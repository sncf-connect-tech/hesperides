package org.hesperides.domain.modules.queries;

import lombok.Value;
import org.axonframework.queryhandling.QueryExecutionException;
import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.domain.modules.entities.Module;

import java.util.concurrent.ExecutionException;

/**
 * test si un module existe déjà ou pas.
 */
@Value
public class ModuleAlreadyExistsQuery {
    Module.Key key;

    public static boolean sendAndWait(Module.Key key, QueryGateway queryGateway) {
        try {
            return queryGateway.send(new ModuleAlreadyExistsQuery(key), Boolean.class).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new QueryExecutionException(e.getMessage(), e);
        }
    }

    public <T> T sendAndWait(QueryGateway queryGateway, Class<T> classOfT) {
        try {
            return queryGateway.send(this, classOfT).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new QueryExecutionException(e.getMessage(), e);
        }
    }
}
