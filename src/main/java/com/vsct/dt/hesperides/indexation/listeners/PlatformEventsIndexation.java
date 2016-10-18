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
import com.vsct.dt.hesperides.applications.PlatformCreatedEvent;
import com.vsct.dt.hesperides.applications.PlatformDeletedEvent;
import com.vsct.dt.hesperides.applications.PlatformUpdatedEvent;
import com.vsct.dt.hesperides.indexation.ElasticSearchIndexationExecutor;
import com.vsct.dt.hesperides.indexation.command.DeletePlatformCommand;
import com.vsct.dt.hesperides.indexation.command.IndexNewPlatformCommand;
import com.vsct.dt.hesperides.indexation.command.UpdateIndexedPlatformCommand;
import com.vsct.dt.hesperides.indexation.mapper.PlatformMapper;
import com.vsct.dt.hesperides.indexation.model.PlatformIndexation;

/**
 * Created by william_montaz on 22/01/2015.
 */
public class PlatformEventsIndexation {

    private final ElasticSearchIndexationExecutor indexer;

    public PlatformEventsIndexation(final ElasticSearchIndexationExecutor indexer) {
        this.indexer = indexer;
    }

    @Subscribe
    public void createPlatform(final PlatformCreatedEvent event) {

        PlatformIndexation platformIndexation = PlatformMapper.asPlatformIndexation(event.getPlatform());

        this.indexer.index(new IndexNewPlatformCommand(platformIndexation));

    }

    @Subscribe
    public void updatePlatform(final PlatformUpdatedEvent event) {

        PlatformIndexation platformIndexation = PlatformMapper.asPlatformIndexation(event.getPlatform());

        this.indexer.index(new UpdateIndexedPlatformCommand(platformIndexation));

    }

    @Subscribe
    public void deletePlatform(final PlatformDeletedEvent event){
        this.indexer.index(new DeletePlatformCommand(event));
    }

}
