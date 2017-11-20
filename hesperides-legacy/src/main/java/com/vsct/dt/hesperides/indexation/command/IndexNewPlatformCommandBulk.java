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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vsct.dt.hesperides.indexation.ElasticSearchClient;
import com.vsct.dt.hesperides.indexation.ElasticSearchIndexationCommand;
import com.vsct.dt.hesperides.indexation.mapper.ElasticSearchMappers;
import com.vsct.dt.hesperides.indexation.mapper.SingleToBulkMapper;
import com.vsct.dt.hesperides.indexation.model.ElasticSearchEntity;
import com.vsct.dt.hesperides.indexation.model.PlatformIndexation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 09/12/2014.
 */
public final class IndexNewPlatformCommandBulk implements ElasticSearchIndexationCommand<PlatformIndexation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexNewPlatformCommandBulk.class);
    private final List<PlatformIndexation> platforms;

    public IndexNewPlatformCommandBulk(final List<PlatformIndexation> platforms) {
        this.platforms = platforms;
    }

    @Override
    public Void index(final ElasticSearchClient elasticSearchClient) {
        String body;
        List<SingleToBulkMapper> propertiesAsString;

        LOGGER.info("Index {} platforms.", platforms.size());

        propertiesAsString = platforms.stream().map(platform -> {
            try {
                LOGGER.info("Index platform with name {}-{}-{}.", platform.getApplicationName(), platform.getPlatformName(), platform
                        .getApplicationVersion());

                return new SingleToBulkMapper(platform.getId(), ElasticSearchMappers.PLATFORM_WRITER.writeValueAsString(platform));
            } catch (final JsonProcessingException e) {
                LOGGER.error("Could not serialize platform " + platform);
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        if (propertiesAsString != null && propertiesAsString.size() > 0) {
            body = propertiesAsString.stream().map(platform -> platform.toString()).collect(Collectors.joining(""));

            ElasticSearchEntity<PlatformIndexation> entity = elasticSearchClient.withResponseReader(ElasticSearchMappers.ES_ENTITY_PLATFORM_READER)
                    .post("/platforms/_bulk", body);

            LOGGER.info("Successfully indexed new platform {}", propertiesAsString.size());
        }
        return null;
    }
}
