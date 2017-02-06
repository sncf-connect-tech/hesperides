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

package com.vsct.dt.hesperides.templating.modules.event;

import com.vsct.dt.hesperides.templating.modules.*;

/**
 * Interface to receive event of module.
 *
 * Created by emeric_martineau on 15/01/2016.
 */
public interface ModuleEventInterface {
    /**
     * Event when module created.
     *
     * @param event event
     */
    void replayModuleCreatedEvent(ModuleCreatedEvent event);

    /**
     * Event when module in working copy update.
     *
     * @param event event
     */
    void replayModuleWorkingCopyUpdatedEvent(ModuleWorkingCopyUpdatedEvent event);

    /**
     * Event when new template add in module.
     *
     * @param event event
     */
    void replayModuleTemplateCreatedEvent(ModuleTemplateCreatedEvent event);

    /**
     * Event when a template in module updated.
     *
     * @param event event
     */
    void replayModuleTemplateUpdatedEvent(ModuleTemplateUpdatedEvent event);

    /**
     * Event when template is remove from module.
     *
     * @param event event
     */
    void replayModuleTemplateDeletedEvent(ModuleTemplateDeletedEvent event);

    /**
     * Event when module is deleted.
     *
     * @param event
     */
    void replayModuleDeletedEvent(ModuleDeletedEvent event);
}
