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
import com.vsct.dt.hesperides.indexation.command.DeleteIndexedTemplateCommand;
import com.vsct.dt.hesperides.indexation.command.DeleteTemplatePackageCommand;
import com.vsct.dt.hesperides.indexation.command.IndexNewTemplateCommand;
import com.vsct.dt.hesperides.indexation.command.UpdateIndexedTemplateCommand;
import com.vsct.dt.hesperides.indexation.mapper.TemplateMapper;
import com.vsct.dt.hesperides.indexation.model.TemplateIndexation;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.packages.TemplateCreatedEvent;
import com.vsct.dt.hesperides.templating.packages.TemplateDeletedEvent;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageDeletedEvent;
import com.vsct.dt.hesperides.templating.packages.TemplateUpdatedEvent;
import com.vsct.dt.hesperides.templating.packages.event.TemplatePackageEventInterface;

/**
 * Created by william_montaz on 22/01/2015.
 */
public class TemplateEventsIndexation implements TemplatePackageEventInterface {

    private final ElasticSearchIndexationExecutor indexer;

    public TemplateEventsIndexation(final ElasticSearchIndexationExecutor indexer) {
        this.indexer = indexer;
    }

    @Subscribe
    @Override
    public void replayTemplateCreatedEvent(final TemplateCreatedEvent event) {
        Template hesperidesTemplate = event.getCreated();
        TemplateIndexation template = TemplateMapper.asTemplateIndexation(hesperidesTemplate);

        this.indexer.index(new IndexNewTemplateCommand(template));
    }

    @Subscribe
    @Override
    public void replayTemplateDeletedEvent(final TemplateDeletedEvent event) {
        this.indexer.index(new DeleteIndexedTemplateCommand(event.getNamespace(), event.getName()));
    }

    @Subscribe
    @Override
    public void replayTemplateUpdatedEvent(final TemplateUpdatedEvent event) {
        Template hesperidesTemplate = event.getUpdated();
        TemplateIndexation template = TemplateMapper.asTemplateIndexation(hesperidesTemplate);

        this.indexer.index(new UpdateIndexedTemplateCommand(template));
    }

    @Subscribe
    @Override
    public void replayTemplatePackageDeletedEvent(final TemplatePackageDeletedEvent event) {
        this.indexer.index(new DeleteTemplatePackageCommand(event));
    }
}
