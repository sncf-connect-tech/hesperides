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

package com.vsct.dt.hesperides.exception.wrapper;

import com.vsct.dt.hesperides.exception.runtime.DuplicateResourceException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
* Created by william_montaz on 09/01/2015.
*/
public final class DuplicateResourceExceptionMapper extends BaseExceptionMapper implements ExceptionMapper<DuplicateResourceException> {

    @Override
    public Response toResponse(final DuplicateResourceException e) {
        return exceptionResponse(Response.Status.CONFLICT, e);
    }
}
