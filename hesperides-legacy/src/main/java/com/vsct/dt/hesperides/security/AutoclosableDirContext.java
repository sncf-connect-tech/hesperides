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

package com.vsct.dt.hesperides.security;

import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

/**
 * Created by william_montaz on 24/02/2015.
 */
public class AutoclosableDirContext extends InitialDirContext implements AutoCloseable {

    protected AutoclosableDirContext(boolean lazy) throws NamingException {
        super(lazy);
    }

    public AutoclosableDirContext() throws NamingException {
    }

    public AutoclosableDirContext(Hashtable<?, ?> environment) throws NamingException {
        super(environment);
    }

    @Override
    public void close() throws NamingException {
        super.close();
    }
}
