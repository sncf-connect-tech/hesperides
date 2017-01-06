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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by william_montaz on 09/01/2015.
 */
public class BaseExceptionMapper {

    protected final Response exceptionResponse(int status, Exception ex){
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ExceptionResponseWrapper(status, ex))
                .build();
    }

    protected final Response exceptionResponse(Response.Status status, Exception ex){
        return exceptionResponse(status.getStatusCode(), ex);
    }
}
