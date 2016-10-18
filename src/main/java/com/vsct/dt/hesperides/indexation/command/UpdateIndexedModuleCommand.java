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
import com.vsct.dt.hesperides.indexation.model.DocWrapper;
import com.vsct.dt.hesperides.indexation.model.ModuleIndexation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by william_montaz on 09/12/2014.
 */
public final class UpdateIndexedModuleCommand implements ElasticSearchIndexationCommand<ModuleIndexation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexNewModuleCommand.class);

    private final ModuleIndexation module;

    public UpdateIndexedModuleCommand(final ModuleIndexation module) {
        this.module = module;
    }


    @Override
    public Void index(final ElasticSearchClient elasticSearchClient) {

        String url = String.format("/modules/%1$s/_update?fields=_source", module.getId());
        String templateAsJson = null;
        try {
            templateAsJson = ElasticSearchMappers.ES_DOC_MODULE_WRITER.writeValueAsString(DocWrapper.of(module));
        } catch (final JsonProcessingException e) {
            LOGGER.error("Could not serialize module "+module);
            throw new RuntimeException(e);
        }

        elasticSearchClient.withResponseReader(ElasticSearchMappers.ES_ENTITY_MODULE_READER).post(url, templateAsJson);

        LOGGER.info("Successfully updated module {}", module);
        return null;
    }
}
