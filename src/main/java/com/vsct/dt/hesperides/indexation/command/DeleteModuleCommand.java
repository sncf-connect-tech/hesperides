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
import com.vsct.dt.hesperides.indexation.ElasticSearchClient;
import com.vsct.dt.hesperides.indexation.ElasticSearchIndexationCommand;
import com.vsct.dt.hesperides.indexation.mapper.ElasticSearchMappers;
import com.vsct.dt.hesperides.indexation.model.ModuleIndexation;
import com.vsct.dt.hesperides.indexation.model.TemplateIndexation;
import com.vsct.dt.hesperides.indexation.search.TemplateSearch;
import com.vsct.dt.hesperides.indexation.search.TemplateSearchResponse;
import com.vsct.dt.hesperides.templating.modules.ModuleDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by william_montaz on 20/04/2015.
 */
public class DeleteModuleCommand implements ElasticSearchIndexationCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteModuleCommand.class);
    private final ModuleIndexation module;

    public DeleteModuleCommand(ModuleDeletedEvent event) {
        module = new ModuleIndexation(event.getModuleName(), event.getModuleVersion(), event.isWorkingCopy(), Lists.newArrayList());
    }

    @Override
    public Void index(ElasticSearchClient elasticSearchClient) {
        String url = String.format("/modules/%1$s", module.getId());
        elasticSearchClient.withResponseReader(ElasticSearchMappers.ES_ENTITY_TEMPLATE_READER).delete(url);

        LOGGER.info("Successfully deleted module {}", module);

        //Remove templates
        TemplateSearch templateSearch = new TemplateSearch(elasticSearchClient);
        Set<TemplateSearchResponse> searchResults = templateSearch.getTemplatesByExactNamespace(module.getNamespace());

        //Reduce to templates having exactly the same namespace
        searchResults.stream().forEach(templateInfo -> {
            /* Not very efficient and safe way to get the id */
            TemplateIndexation template = new TemplateIndexation(templateInfo.getNamespace(), templateInfo.getName(), null, null);
            String templateurl = String.format("/templates/%1$s", template.getId());
            elasticSearchClient.withResponseReader(ElasticSearchMappers.ES_ENTITY_TEMPLATE_READER).delete(templateurl);
        });
        return null;
    }
}
