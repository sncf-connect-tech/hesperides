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

package com.vsct.dt.hesperides.util.converter;

import com.vsct.dt.hesperides.resources.KeyValueValorisation;
import com.vsct.dt.hesperides.templating.platform.KeyValueValorisationData;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by emeric_martineau on 28/10/2015.
 */
public abstract class AbstractKeyConverter {
    protected abstract KeyValueValorisationConverter getKeyConverter();

    /**
     * Convert list of KeyValueValorisation to list of KeyValueValorisationData.
     * @param list input
     * @return output
     */
    protected Set<KeyValueValorisationData> toListKeyValueValorisationData(final Set<KeyValueValorisation> list) {
        Set<KeyValueValorisationData> newList = new HashSet<>(list.size());

        for (KeyValueValorisation kv : list) {
            newList.add(getKeyConverter().toKeyValueValorisationData(kv));
        }

        return newList;
    }

    /**
     * Convert list of KeyValueValorisationData to list of KeyValueValorisation.
     * @param list input
     * @return output
     */
    protected Set<KeyValueValorisation> toListKeyValueValorisation(final Set<KeyValueValorisationData> list) {
        Set<KeyValueValorisation> newList = new HashSet<>(list.size());

        for (KeyValueValorisationData kv : list) {
            newList.add(getKeyConverter().toKeyValueValorisation(kv));
        }

        return newList;
    }
}
