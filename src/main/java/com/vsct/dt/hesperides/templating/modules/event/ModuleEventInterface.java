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
