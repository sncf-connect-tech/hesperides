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
import com.vsct.dt.hesperides.resources.Properties;
import com.vsct.dt.hesperides.templating.platform.IterableValorisationData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;
import com.vsct.dt.hesperides.util.converter.AbstractKeyConverter;
import com.vsct.dt.hesperides.util.converter.IterableValorisationConverter;
import com.vsct.dt.hesperides.util.converter.KeyValueValorisationConverter;
import com.vsct.dt.hesperides.util.converter.PropertiesConverter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by emeric_martineau on 28/10/2015.
 */
public class DefaultPropertiesConverter extends AbstractKeyConverter implements PropertiesConverter {

    private KeyValueValorisationConverter keyValueValorisationConverter;
    private IterableValorisationConverter iterableValorisationConverter;

    public DefaultPropertiesConverter() {
        this.keyValueValorisationConverter = new DefaultKeyValueValorisationConverter();
        this.iterableValorisationConverter = new DefaultIterableValorisationConverter();
    }

    public DefaultPropertiesConverter(final KeyValueValorisationConverter converter1,
                                      final IterableValorisationConverter converter2) {
        this.keyValueValorisationConverter = converter1;
        this.iterableValorisationConverter = converter2;
    }

    @Override
    public Properties toProperties(final PropertiesData prop) {
        return new Properties(toListKeyValueValorisation(prop.getKeyValueProperties()),
                toListIterableValorisation(prop.getIterableProperties()));
    }

    @Override
    public PropertiesData toPropertiesData(final Properties prop) {
        return new PropertiesData(toListKeyValueValorisationData(prop.getKeyValueProperties()),
                toListIterableValorisationData(prop.getIterableProperties()));
    }

    @Override
    protected KeyValueValorisationConverter getKeyConverter() {
        return keyValueValorisationConverter;
    }

    /**
     * Convert list of IterableValorisation to list of IterableValorisationData.
     *
     * @param list input
     * @return output
     */
    protected Set<IterableValorisationData> toListIterableValorisationData(final Set<IterableValorisation> list) {
        Set<IterableValorisationData> newList = new HashSet<>(list.size());

        for (IterableValorisation iv : list) {
            newList.add(iterableValorisationConverter.toIterableValorisationData(iv));
        }

        return newList;
    }

    /**
     * Convert list of IterableValorisationData to list of IterableValorisation.
     *
     * @param list input
     * @return output
     */
    protected Set<IterableValorisation> toListIterableValorisation(final Set<IterableValorisationData> list) {
        Set<IterableValorisation> newList = new HashSet<>(list.size());

        for (IterableValorisationData iv : list) {
            newList.add(iterableValorisationConverter.toIterableValorisation(iv));
        }

        return newList;
    }
}
