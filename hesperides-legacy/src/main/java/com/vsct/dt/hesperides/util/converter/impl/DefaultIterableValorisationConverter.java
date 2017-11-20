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

package com.vsct.dt.hesperides.util.converter.impl;

import com.vsct.dt.hesperides.resources.IterableValorisation;
import com.vsct.dt.hesperides.resources.KeyValueValorisation;
import com.vsct.dt.hesperides.resources.Valorisation;
import com.vsct.dt.hesperides.templating.platform.IterableValorisationData;
import com.vsct.dt.hesperides.templating.platform.KeyValueValorisationData;
import com.vsct.dt.hesperides.templating.platform.ValorisationData;
import com.vsct.dt.hesperides.util.converter.IterableValorisationConverter;
import com.vsct.dt.hesperides.util.converter.KeyValueValorisationConverter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by emeric_martineau on 28/10/2015.
 */
public class DefaultIterableValorisationConverter implements IterableValorisationConverter {
    private KeyValueValorisationConverter keyValueValorisationConverter = new DefaultKeyValueValorisationConverter();

    public DefaultIterableValorisationConverter() {
        this.keyValueValorisationConverter = new DefaultKeyValueValorisationConverter();
    }

    public DefaultIterableValorisationConverter(final KeyValueValorisationConverter converter) {
        this.keyValueValorisationConverter = converter;
    }

    @Override
    public IterableValorisation toIterableValorisation(final IterableValorisationData val) {
        return new IterableValorisation(val.getName(), toListItem(val.getIterableValorisationItems())
        );
    }

    @Override
    public IterableValorisationData toIterableValorisationData(final IterableValorisation val) {
        return new IterableValorisationData(val.getName(), toListItemData(val.getIterableValorisationItems()));
    }

    @Override
    public IterableValorisationData.IterableValorisationItemData toIterableValorisationItemData(final IterableValorisation.IterableValorisationItem item) {
        return new IterableValorisationData.IterableValorisationItemData(item.getTitle(),
                toListValorisationData(item.getValues()));
    }

    @Override
    public IterableValorisation.IterableValorisationItem toIterableValorisationItem(final IterableValorisationData.IterableValorisationItemData item) {
        return new IterableValorisation.IterableValorisationItem(item.getTitle(),
                toListValorisation(item.getValues()));

    }

    /**
     * Convert list of Valorisation to list of ValorisationData.
     *
     * @param list inpput
     * @return output
     */
    private Set<ValorisationData> toListValorisationData(final Set<Valorisation> list) {
        Set<ValorisationData> newList = new HashSet<>(list.size());

        for (Valorisation val : list) {
            if (val instanceof KeyValueValorisation) {
                newList.add(keyValueValorisationConverter.toKeyValueValorisationData((KeyValueValorisation) val));
            } else {
                newList.add(toIterableValorisationData((IterableValorisation) val));
            }
        }

        return newList;
    }

    /**
     * Convert list of Valorisation to list of ValorisationData.
     *
     * @param list inpput
     * @return output
     */
    private Set<Valorisation> toListValorisation(final Set<ValorisationData> list) {
        Set<Valorisation> newList = new HashSet<>(list.size());

        for (ValorisationData val : list) {
            if (val instanceof KeyValueValorisationData) {
                newList.add(keyValueValorisationConverter.toKeyValueValorisation((KeyValueValorisationData) val));
            } else {
                newList.add(toIterableValorisation((IterableValorisationData) val));
            }
        }

        return newList;
    }

    /**
     * Convert list of IterableValorisationData.IterableValorisationItemData to list of IterableValorisation.IterableValorisationItem.
     * @param list input
     * @return outpur
     */
    private List<IterableValorisationData.IterableValorisationItemData> toListItemData(final List<IterableValorisation.IterableValorisationItem> list) {
        List<IterableValorisationData.IterableValorisationItemData> newList = new ArrayList<>(list.size());

        for (IterableValorisation.IterableValorisationItem item : list) {
            newList.add(toIterableValorisationItemData(item));
        }

        return newList;
    }

    /**
     * Convert list of IterableValorisation.IterableValorisationItem to list of IterableValorisationData.IterableValorisationItemData.
     * @param list input
     * @return outpur
     */
    private List<IterableValorisation.IterableValorisationItem> toListItem(final List<IterableValorisationData.IterableValorisationItemData> list) {
        List<IterableValorisation.IterableValorisationItem> newList = new ArrayList<>(list.size());

        for (IterableValorisationData.IterableValorisationItemData item : list) {
            newList.add(toIterableValorisationItem(item));
        }

        return newList;
    }
}
