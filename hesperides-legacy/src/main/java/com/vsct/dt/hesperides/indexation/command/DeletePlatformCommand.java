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

package com.vsct.dt.hesperides.indexation.command;

import com.google.common.collect.Lists;
import com.vsct.dt.hesperides.applications.PlatformDeletedEvent;
import com.vsct.dt.hesperides.indexation.ElasticSearchClient;
import com.vsct.dt.hesperides.indexation.ElasticSearchIndexationCommand;
import com.vsct.dt.hesperides.indexation.mapper.ElasticSearchMappers;
import com.vsct.dt.hesperides.indexation.model.PlatformIndexation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by william_montaz on 20/04/2015.
 */
public class DeletePlatformCommand implements ElasticSearchIndexationCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeletePlatformCommand.class);
    private final PlatformIndexation platform;

    public DeletePlatformCommand(PlatformDeletedEvent event) {
        platform = new PlatformIndexation(event.getPlatformName(), event.getApplicationName(), "", Lists.newArrayList());
    }

    @Override
    public Void index(ElasticSearchClient elasticSearchClient) {
        String url = String.format("/platforms/%1$s", platform.getId());
        elasticSearchClient.withResponseReader(ElasticSearchMappers.ES_ENTITY_TEMPLATE_READER).delete(url);

        LOGGER.info("Successfully deleted paltform {}", platform);
        return null;
    }
}
