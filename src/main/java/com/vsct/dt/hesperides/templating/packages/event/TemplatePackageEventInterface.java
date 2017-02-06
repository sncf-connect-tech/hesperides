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

package com.vsct.dt.hesperides.templating.packages.event;

import com.vsct.dt.hesperides.templating.packages.TemplateCreatedEvent;
import com.vsct.dt.hesperides.templating.packages.TemplateDeletedEvent;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageDeletedEvent;
import com.vsct.dt.hesperides.templating.packages.TemplateUpdatedEvent;

/**
 * Created by emeric_martineau on 19/01/2016.
 */
public interface TemplatePackageEventInterface {
    void replayTemplateCreatedEvent(final TemplateCreatedEvent event);

    void replayTemplateUpdatedEvent(final TemplateUpdatedEvent event);

    void replayTemplateDeletedEvent(final TemplateDeletedEvent event);

    void replayTemplatePackageDeletedEvent(final TemplatePackageDeletedEvent event);
}
