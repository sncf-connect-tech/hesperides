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
package org.hesperides.test.bdd.commons;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HesperidesScenario {

    @Autowired
    protected TestContext testContext;

    protected <T> T[] getBodyAsArray() {
        return ((ResponseEntity<T[]>) testContext.responseEntity).getBody();
    }

    protected Map getBodyAsMap() {
        return ((ResponseEntity<Map>) testContext.responseEntity).getBody();
    }

    public static Class getResponseType(String tryTo, Class defaultResponseType) {
        return StringUtils.isEmpty(tryTo) ? defaultResponseType : String.class;
    }

    public void assertOK() {
        assertEquals(HttpStatus.OK, testContext.responseEntity.getStatusCode());
    }

    public void assertCreated() {
        assertEquals(HttpStatus.CREATED, testContext.responseEntity.getStatusCode());
    }

    public void assertNotFound() {
        assertEquals(HttpStatus.NOT_FOUND, testContext.responseEntity.getStatusCode());
    }

    public void assertConflict() {
        assertEquals(HttpStatus.CONFLICT, testContext.responseEntity.getStatusCode());
    }

    public void assertMethodNotAllowed() {
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, testContext.responseEntity.getStatusCode());
    }

    public void assertBadRequest() {
        assertEquals(HttpStatus.BAD_REQUEST, testContext.responseEntity.getStatusCode());
    }

    public void assertNoContent() {
        assertEquals(HttpStatus.NO_CONTENT, testContext.responseEntity.getStatusCode());
    }

    public void assertInternalServerError() {
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, testContext.responseEntity.getStatusCode());
    }
}
