/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.commons.axon;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryExecutionException;
import org.axonframework.queryhandling.QueryGateway;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public abstract class AxonQueries {

    private final QueryGateway queryGateway;

    protected AxonQueries(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    protected <R> R querySync(Object query, Class<R> responseType) {
        try {
            return queryGateway.query(query, ResponseTypes.instanceOf(responseType)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new QueryExecutionException(e.getMessage(), e);
        }
    }

    protected <R> Optional<R> querySyncOptional(Object query, Class<R> responseType) {
        try {
            return queryGateway.query(query, ResponseTypes.optionalInstanceOf(responseType)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new QueryExecutionException(e.getMessage(), e);
        }
    }

    protected <R> List<R> querySyncList(Object query, Class<R> responseType) {
        try {
            return queryGateway.query(query, ResponseTypes.multipleInstancesOf(responseType)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new QueryExecutionException(e.getMessage(), e);
        }
    }
}
