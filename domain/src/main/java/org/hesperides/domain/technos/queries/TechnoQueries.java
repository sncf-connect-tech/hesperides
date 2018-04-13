package org.hesperides.domain.technos.queries;

import org.axonframework.queryhandling.QueryExecutionException;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.responsetypes.ResponseTypes;
import org.hesperides.domain.framework.OptionalResponseType;
import org.hesperides.domain.technos.entities.Techno;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Permet de regrouper les queries
 */
@Component
public class TechnoQueries {

    private final QueryGateway queryGateway;

    public TechnoQueries(QueryGateway queryGateway) {
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

    public boolean technoExist(Techno.Key newTechnoKey) {
        throw new IllegalArgumentException("TODO");
        //return querySync(new TechnoAlreadyExistsQuery(newTechnoKey), Boolean.class);
    }
}
