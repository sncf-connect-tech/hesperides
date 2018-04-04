/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.infrastructure.elasticsearch.modules;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.elasticsearch.client.Response;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleRepository;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.infrastructure.elasticsearch.ElasticsearchService;
import org.hesperides.infrastructure.views.TrackedProjection;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@TrackedProjection
@Profile("!local")
public class ElasticsearchModuleSearchRepository implements ModuleRepository {

    private static final String SEARCH_MODULE_NAME_VERSION_WORKINGCOPY_MUSTACHE = "search.module.name.version.workingcopy.mustache";
    private static final String MUSTACHE_SEARCH_ALL = "search.module.all.mustache";

    private final ElasticsearchService elasticsearchService;

    private static final int POSITIVE_MASK = 0x7FFFFFFF;//-> 0111 1111 1111 1111.... thus removes the first bit

    public ElasticsearchModuleSearchRepository(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    @Override
    @QueryHandler
    public Optional<ModuleView> query(ModuleByIdQuery query) {
        return elasticsearchService
                .getOne(hashOf(query.getModuleKey()), ModuleIndexation.class)
                .map(ModuleIndexation::toModuleView);
    }

    @Override
    @QueryHandler
    public List<String> queryAllModuleNames(ModulesNamesQuery query) {
        return elasticsearchService
                .searchForSome("modules", MUSTACHE_SEARCH_ALL, ModuleIndexation.class)
                .stream().map(ModuleIndexation::getName).collect(Collectors.toList());
    }

    @Override
    @QueryHandler
    public List<String> queryModuleTypes(ModuleTypesQuery query) {
        return elasticsearchService
                .searchForSome("modules", MUSTACHE_SEARCH_ALL, ModuleIndexation.class)
                .stream()
                .filter(module -> module.getName().equalsIgnoreCase(query.getModuleName()))
                .filter(module -> module.getVersion().equalsIgnoreCase(query.getModuleVersion()))
                .map(ModuleIndexation::toModuleTypeView)
                .collect(Collectors.toList());
    }

    @Override
    @QueryHandler
    public List<String> queryModuleVersions(ModuleVersionsQuery query) {
        return elasticsearchService
                .searchForSome("modules", MUSTACHE_SEARCH_ALL, ModuleIndexation.class)
                .stream()
                .filter(module -> module.getName().equalsIgnoreCase(query.getModuleName()))
                .map(ModuleIndexation::getVersion).collect(Collectors.toList());
    }

    @Override
    @QueryHandler
    public Boolean query(ModuleAlreadyExistsQuery query) {
        return query(new ModuleByIdQuery(query.getModuleKey())).isPresent();
    }

    /**
     * indexation
     *
     * @param event
     */
    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void indexCreatedModule(ModuleCreatedEvent event) throws IOException {

        Response modules = elasticsearchService.index(hashOf(event.getModule().getKey()), new ModuleIndexation(
                        event.getModule().getKey().getName(),
                        event.getModule().getKey().getVersion(),
                        event.getModule().getKey().isWorkingCopy(),
                        ImmutableList.of(),
                        event.getModule().getVersionId()
                )
        );

        log.debug("nouveau module indexé ? {}, {}", modules.getStatusLine(),
                EntityUtils.toString(modules.getEntity()));
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void indexUpdatedModule(ModuleUpdatedEvent event) throws IOException {

        Response modules = elasticsearchService.index(hashOf(event.getModule().getKey()), new ModuleIndexation(
                        event.getModule().getKey().getName(),
                        event.getModule().getKey().getVersion(),
                        event.getModule().getKey().isWorkingCopy(),
                        ImmutableList.of(),
                        event.getModule().getVersionId()
                )
        );

        log.debug("maj module indexé ? {}, {}", modules.getStatusLine(),
                EntityUtils.toString(modules.getEntity()));
    }

    @EventSourcingHandler
    @SuppressWarnings("unused")
    public void removeDeletedModule(ModuleDeletedEvent event) throws IOException {

        Response modules = elasticsearchService.delete(hashOf(event.getModule().getKey()));

        log.debug("suppression module ? {}, {}", modules.getStatusLine(),
                EntityUtils.toString(modules.getEntity()));
    }

    private String hashOf(Module.Key key) {
        int hash = Objects.hash(key.getName(), key.getVersion(), key.isWorkingCopy());
        hash = hash & POSITIVE_MASK;
        return "modules/" + hash;
    }

}
