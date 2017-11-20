/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides;

import com.vsct.dt.hesperides.exception.runtime.NonUniqueResultException;
import com.vsct.dt.hesperides.indexation.model.Data;
import com.vsct.dt.hesperides.indexation.model.ElasticSearchEntity;
import com.vsct.dt.hesperides.indexation.model.ElasticSearchResponse;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by william on 04/09/2014.
 */
public class ElasticSearchMock {

    /* ELASTIC SEARCH SEARCH RESPONSE */
    public static <T> ElasticSearchResponse<T> elasticSearchResponseWithEntity(T entity) {
        ElasticSearchResponse<T> elasticSearchResponse = mock(ElasticSearchResponse.class);
        try {
            when(elasticSearchResponse.getSingleResult()).thenReturn(Optional.of(entity));
        } catch (NonUniqueResultException e) {
            e.printStackTrace();
        }
        return elasticSearchResponse;
    }

    public static <T> ElasticSearchResponse<T> elasticSearchResponseWithEntities(T... entities) {
        ElasticSearchResponse<T> elasticSearchResponse = mock(ElasticSearchResponse.class);
        try {
            when(elasticSearchResponse.streamOfData()).thenReturn(Arrays.asList(entities).stream());
            when(elasticSearchResponse.getHitsNumber()).thenReturn((long) entities.length);
        } catch (NonUniqueResultException e) {
            e.printStackTrace();
        }
        return elasticSearchResponse;
    }

    public static <T> ElasticSearchResponse<T> emptyElasticSearchResponse() {
        ElasticSearchResponse<T> elasticSearchResponse = mock(ElasticSearchResponse.class);
        try {
            when(elasticSearchResponse.getSingleResult()).thenReturn(Optional.empty());
        } catch (NonUniqueResultException e) {
            e.printStackTrace();
        }
        return elasticSearchResponse;
    }

    public static <T> ElasticSearchResponse<T> nonUniqueResultElasticSearchResponse() {
        ElasticSearchResponse<T> elasticSearchResponse = mock(ElasticSearchResponse.class);
        try {
            when(elasticSearchResponse.getSingleResult()).thenThrow(new NonUniqueResultException("message"));
        } catch (NonUniqueResultException e) {
            e.printStackTrace();
        }
        return elasticSearchResponse;
    }

}
