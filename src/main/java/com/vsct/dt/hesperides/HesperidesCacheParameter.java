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

package com.vsct.dt.hesperides;

import javax.validation.Valid;

/**
 * Created by emeric_martineau on 28/01/2016.
 */
public class HesperidesCacheParameter {
    public static final int NOT_SET = -1;

    /**
     * Maximum size in cache.
     */
    @Valid
    private int maxSize = NOT_SET;

    /**
     * Expire time.
     */
    @Valid
    private String itemExpireAfter = null;

    /**
     * Maximum weight.
     */
    @Valid
    private int weight = NOT_SET;

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(final int maxSize) {
        this.maxSize = maxSize;
    }

    public String getItemExpireAfter() {
        return itemExpireAfter;
    }

    public void setItemExpireAfter(final String itemExpireAfter) {
        this.itemExpireAfter = itemExpireAfter;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(final int weight) {
        this.weight = weight;
    }
}
