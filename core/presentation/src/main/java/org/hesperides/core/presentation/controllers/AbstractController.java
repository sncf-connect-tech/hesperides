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

package org.hesperides.core.presentation.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

@CrossOrigin
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
abstract class AbstractController {

//    final protected <E, T> Response entityWithConverterOrNotFound(final Optional<T> optional, ResponseConverter<T, E> converter) {
//        return optional.map(t -> Response.ok(converter.convert(t)).build()).orElseThrow(() -> new MissingResourceException("Requested entity is missing"));
//    }

    /**
     * Check if char is lower space.
     *
     * @param str string
     * @return true/false
     */
    public static boolean isNonDisplayedChar(String str) {
        int strLen = str == null ? 0 : str.length();
        for (int i = 0; i < strLen; i++) {
            if (str.charAt(i) < 0x20) {
                return true;
            }
        }

        return false;
    }

    final protected void checkQueryParameterNotEmpty(final String paramName, final String param) {
        // Utiliser l'annotation ?
        if (param == null || isNonDisplayedChar(param)) {
            throw new IllegalArgumentException("Query parameter " + paramName + " is missing");
        }
    }

    final protected void checkQueryParameterNotEmpty(final String paramName, final Object param) {
        if (param == null) {
            throw new IllegalArgumentException("Query parameter " + paramName + " is missing");
        }
    }
}
