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
package org.hesperides.tests.bddrefacto.commons;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class StepHelper {

    public static Class getResponseType(final String tryTo, Class defaultResponseType) {
        return StringUtils.isEmpty(tryTo) ? defaultResponseType : String.class;
    }

    public static void assertOK(ResponseEntity responseEntity) {
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    public static void assertCreated(ResponseEntity responseEntity) {
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    public static void assertNotFound(ResponseEntity responseEntity) {
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    public static void assertConflict(ResponseEntity responseEntity) {
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
    }

    public static void assertMethodNotAllowed(ResponseEntity responseEntity) {
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, responseEntity.getStatusCode());
    }

    public static void assertBadRequest(ResponseEntity responseEntity) {
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }
}
