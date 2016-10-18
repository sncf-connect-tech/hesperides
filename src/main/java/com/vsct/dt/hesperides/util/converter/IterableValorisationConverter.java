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

import com.vsct.dt.hesperides.resources.IterableValorisation;
import com.vsct.dt.hesperides.templating.platform.IterableValorisationData;

/**
 * Created by emeric_martineau on 28/10/2015.
 */
public interface IterableValorisationConverter {
    /**
     * Convert Valorisation to ValorisationData.
     * @param val input
     * @return output
     */
    IterableValorisation toIterableValorisation(IterableValorisationData val);

    /**
     * Convert IterableValorisation to IterableValorisationData.
     * @param val input
     * @return output
     */
    IterableValorisationData toIterableValorisationData(IterableValorisation val);

    /**
     * Convert IterableValorisationData.IterableValorisationItemData to IterableValorisation.IterableValorisationItem.
     * @param item input
     * @return output
     */
    IterableValorisationData.IterableValorisationItemData toIterableValorisationItemData(IterableValorisation.IterableValorisationItem item);

    /**
     * Convert IterableValorisatio.IterableValorisationItem to IterableValorisationData.IterableValorisationItemData.
     * @param item input
     * @return output
     */
    IterableValorisation.IterableValorisationItem toIterableValorisationItem(IterableValorisationData.IterableValorisationItemData item);
}
