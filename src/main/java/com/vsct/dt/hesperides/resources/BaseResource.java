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

package com.vsct.dt.hesperides.resources;

import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;

import javax.ws.rs.core.Response;
import java.util.Optional;

import static com.vsct.dt.hesperides.util.CheckArgument.isNonDisplayedChar;

/**
 * Created by william_montaz on 16/07/14.
 */
public abstract class BaseResource {

    final protected <E, T> Response entityWithConverterOrNotFound(final Optional<T> optional, ResponseConverter<T, E> converter) {
        return optional.map(t -> Response.ok(converter.convert(t)).build()).orElseThrow(() -> new MissingResourceException("Requested entity is missing"));
    }

    final protected void checkQueryParameterNotEmpty(final String paramName, final String param) {
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
