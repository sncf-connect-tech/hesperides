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
import com.vsct.dt.hesperides.indexation.model.ModuleIndexation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 09/12/2014.
 */
public final class IndexNewModuleCommandBulk implements ElasticSearchIndexationCommand<ModuleIndexation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexNewModuleCommandBulk.class);

    private final List<ModuleIndexation> modules;

    public IndexNewModuleCommandBulk(final List<ModuleIndexation> modules) { this.modules = modules; }

    @Override
    public Void index(final ElasticSearchClient elasticSearchClient) {
        String body;
        List<SingleToBulkMapper> propertiesAsString;

        LOGGER.info("Index {} modules.", modules.size());

        propertiesAsString = modules.stream().map(module -> {
            LOGGER.info("Index module with namespace {}.", module.getNamespace());

            try {
                return new SingleToBulkMapper(module.getId(), ElasticSearchMappers.MODULE_WRITER.writeValueAsString(module));
            } catch (final JsonProcessingException e) {
                LOGGER.error("Could not serialize module " + module);
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        if (propertiesAsString != null && propertiesAsString.size() > 0) {
            body = propertiesAsString.stream().map(module -> module.toString()).collect(Collectors.joining(""));
            elasticSearchClient.withResponseReader(ElasticSearchMappers.ES_ENTITY_MODULE_READER)
                    .post("/modules/_bulk", body);

            LOGGER.info("Successfully indexed new modules {}", propertiesAsString.size());
        }

        return null;
    }
}
