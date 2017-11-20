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

import com.vsct.dt.hesperides.applications.*;
import com.vsct.dt.hesperides.applications.event.PlatformEventBuilderInterface;

import com.vsct.dt.hesperides.indexation.ElasticSearchIndexationExecutor;
import com.vsct.dt.hesperides.indexation.command.DeletePlatformCommand;
import com.vsct.dt.hesperides.indexation.command.IndexNewPlatformCommand;
import com.vsct.dt.hesperides.indexation.command.UpdateIndexedPlatformCommand;
import com.vsct.dt.hesperides.indexation.mapper.PlatformMapper;
import com.vsct.dt.hesperides.indexation.model.PlatformIndexation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by william_montaz on 22/01/2015.
 */
public class PlatformEventsIndexation implements PlatformEventBuilderInterface {

    private final ElasticSearchIndexationExecutor indexer;
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformEventsIndexation.class);

    public PlatformEventsIndexation(final ElasticSearchIndexationExecutor indexer) {
        this.indexer = indexer;
    }

    @Subscribe
    @Override
    public void replayPlatformCreatedEvent(final PlatformCreatedEvent event) {

        PlatformIndexation platformIndexation = PlatformMapper.asPlatformIndexation(event.getPlatform());

        this.indexer.index(new IndexNewPlatformCommand(platformIndexation));
        LOGGER.debug("Indexing new platform : [" + platformIndexation.getApplicationName() + "-" + platformIndexation.getPlatformName() + "]");
    }

    @Subscribe
    @Override
    public void replayPlatformCreatedFromExistingEvent(final PlatformCreatedFromExistingEvent event) {

        PlatformIndexation platformIndexation = PlatformMapper.asPlatformIndexation(event.getPlatform());

        this.indexer.index(new IndexNewPlatformCommand(platformIndexation));
        LOGGER.debug("Indexing new platform : [" + platformIndexation.getApplicationName() + "-" + platformIndexation.getPlatformName() + "] from [" + event.getOriginPlatform().getApplicationName() + "-" + event.getOriginPlatform().getPlatformName() + "]");
    }

    @Subscribe
    @Override
    public void replayPlatformUpdatedEvent(final PlatformUpdatedEvent event) {

        PlatformIndexation platformIndexation = PlatformMapper.asPlatformIndexation(event.getPlatform());

        this.indexer.index(new UpdateIndexedPlatformCommand(platformIndexation));
        LOGGER.debug("Updating platform : [" + platformIndexation.getApplicationName() + "-" + platformIndexation.getPlatformName() + "]");
    }

    @Override
    public void replayPropertiesSavedEvent(PropertiesSavedEvent event) {

    }

    @Subscribe
    @Override
    public void replayPlateformeDeletedEvent(final PlatformDeletedEvent event){
        this.indexer.index(new DeletePlatformCommand(event));
        LOGGER.debug("Deleting platform platform : [" + event.getApplicationName() + "-" + event.getPlatformName() + "]");
    }

    @Override
    public void replaySnapshotTakenEvent(PlatformSnapshotEvent event) {

    }

    @Override
    public void replaySnapshotRestoredEvent(PlatformSnapshotRestoreEvent event) {

    }

}
