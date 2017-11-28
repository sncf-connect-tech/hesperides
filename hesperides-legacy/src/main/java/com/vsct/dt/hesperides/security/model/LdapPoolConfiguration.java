/*
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
 */
package com.vsct.dt.hesperides.security.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by emeric_martineau on 16/05/2017.
 * <p>
 * All data is requiered
 */
public class LdapPoolConfiguration {
    /**
     * Initial size of pool.
     */
    @JsonProperty
    private int initsize = 5;

    /**
     * Max size of pool.
     */
    @JsonProperty
    private int maxsize = 20;

    /**
     * integer that represents the number of milliseconds that an idle connection may remain in the pool without being closed and removed
     * from the pool.
     */
    @JsonProperty
    private int idleTimeout = 6000;

    public int getInitsize() {
        return initsize;
    }

    public void setInitsize(final int initsize) {
        this.initsize = initsize;
    }

    public int getMaxsize() {
        return maxsize;
    }

    public void setMaxsize(final int maxsize) {
        this.maxsize = maxsize;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(final int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }
}
