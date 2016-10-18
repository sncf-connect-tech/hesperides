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

package com.vsct.dt.hesperides.indexation.listeners;

import com.google.common.eventbus.Subscribe;
import com.vsct.dt.hesperides.indexation.ElasticSearchIndexationExecutor;
import com.vsct.dt.hesperides.indexation.command.*;
import com.vsct.dt.hesperides.indexation.mapper.ModuleMapper;
import com.vsct.dt.hesperides.indexation.mapper.TemplateMapper;
import com.vsct.dt.hesperides.indexation.model.ModuleIndexation;
import com.vsct.dt.hesperides.indexation.model.TemplateIndexation;
import com.vsct.dt.hesperides.templating.Template;
import com.vsct.dt.hesperides.templating.modules.*;

/**
 * Created by william_montaz on 22/01/2015.
 */
public class ModuleEventsIndexation {

    private final ElasticSearchIndexationExecutor indexer;

    public ModuleEventsIndexation(final ElasticSearchIndexationExecutor indexer) {
        this.indexer = indexer;
    }

    @Subscribe
    public void createModule(final ModuleCreatedEvent event) {

        ModuleIndexation module = ModuleMapper.toModuleIndexation(event.getModuleCreated());

        this.indexer.index(new IndexNewModuleCommand(module));

        event.getTemplates().forEach(template -> {
            TemplateIndexation templateIndexation = TemplateMapper.asTemplateIndexation(template);
            this.indexer.index(new IndexNewTemplateCommand(templateIndexation));
        });

    }

    @Subscribe
    public void createTemplate(final ModuleTemplateCreatedEvent event) {
        Template hesperidesTemplate = event.getCreated();
        TemplateIndexation template = TemplateMapper.asTemplateIndexation(hesperidesTemplate);

        this.indexer.index(new IndexNewTemplateCommand(template));
    }

    @Subscribe
    public void deleteTemplate(final ModuleTemplateDeletedEvent event) {
        String namespace = ModuleIndexation.getNamespace(event.getModuleName(), event.getModuleVersion(), true);
        this.indexer.index(new DeleteIndexedTemplateCommand(namespace, event.getTemplateName()));
    }

    @Subscribe
    public void updateTemplate(final ModuleTemplateUpdatedEvent event) {
        Template hesperidesTemplate = event.getUpdated();
        TemplateIndexation template = TemplateMapper.asTemplateIndexation(hesperidesTemplate);

        this.indexer.index(new UpdateIndexedTemplateCommand(template));
    }

    @Subscribe
    public void updateWorkingCopy(final ModuleWorkingCopyUpdatedEvent event) {
        ModuleIndexation module = ModuleMapper.toModuleIndexation(event.getUpdated());
        this.indexer.index(new UpdateIndexedModuleCommand(module));
    }

    @Subscribe
    public void deleteModule(final ModuleDeletedEvent event){
        this.indexer.index(new DeleteModuleCommand(event));
    }
}
