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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by william_montaz on 09/01/2015.
 */
public class ExceptionResponseWrapper {
    private int    status;
    private String exception;
    private String message;
    private String stacktrace;

    public ExceptionResponseWrapper(int status, Exception exception) {
        this.status = status;
        this.exception = exception.getClass().getCanonicalName();
        this.message = exception.getMessage();
        StringWriter stacktrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stacktrace));
        this.stacktrace = stacktrace.toString();
    }

    public int getStatus() {
        return status;
    }

    public String getException() {
        return exception;
    }

    public String getMessage() {
        return message;
    }

    public String getStacktrace() {
        return stacktrace;
    }
}
