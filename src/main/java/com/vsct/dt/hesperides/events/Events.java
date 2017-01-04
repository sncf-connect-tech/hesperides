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

package com.vsct.dt.hesperides.events;

import com.vsct.dt.hesperides.storage.Event;

import java.util.List;
import java.util.Set;

/**
 * Event aggragator interface.
 *
 * Created by tidiane_sidibe on 01/03/2016.
 */
public interface Events {
    /**
     * Gets the list of the events for the specified stream
     * @param streamName : the full name of the stream
     * @param page     : Pagination page current page number
     * @param size       : the size of the pagination
     * @return {@code Set<EventData>} a set of event data
     */
    List<EventData> getEventsList(final String streamName, final int page, final int size);
}
