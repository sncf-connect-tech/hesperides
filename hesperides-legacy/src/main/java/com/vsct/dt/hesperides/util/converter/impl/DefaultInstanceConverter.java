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

import com.vsct.dt.hesperides.resources.Instance;
import com.vsct.dt.hesperides.templating.platform.InstanceData;
import com.vsct.dt.hesperides.util.converter.AbstractKeyConverter;
import com.vsct.dt.hesperides.util.converter.InstanceConverter;
import com.vsct.dt.hesperides.util.converter.KeyValueValorisationConverter;

/**
 * Created by emeric_martineau on 27/10/2015.
 */
public final class DefaultInstanceConverter extends AbstractKeyConverter implements InstanceConverter {
    private KeyValueValorisationConverter keyValueValorisationConverter;

    public DefaultInstanceConverter() {
        this.keyValueValorisationConverter = new DefaultKeyValueValorisationConverter();
    }

    public DefaultInstanceConverter(final KeyValueValorisationConverter converter) {
        this.keyValueValorisationConverter = converter;
    }

    /**
     * Convert Instance object to InstanceData
     *
     * @param instance
     * @return
     */
    @Override
    public InstanceData toInstanceData(final Instance instance) {
        return InstanceData
                .withInstanceName(instance.getName())
                .withKeyValue(toListKeyValueValorisationData(instance.getKeyValues()))
                .build();
    }

    @Override
    public Instance toInstance(final InstanceData instance) {
        return new Instance(instance.getName(), toListKeyValueValorisation(instance.getKeyValues()));
    }


    @Override
    protected KeyValueValorisationConverter getKeyConverter() {
        return keyValueValorisationConverter;
    }
}
