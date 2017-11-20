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

package com.vsct.dt.hesperides.resources;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.codahale.metrics.annotation.Timed;
import com.vsct.dt.hesperides.applications.Applications;
import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.Modules;
import com.vsct.dt.hesperides.templating.packages.TemplatePackages;
import com.vsct.dt.hesperides.templating.platform.PlatformData;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.jackson.JsonSnakeCase;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by tidiane_sidibe on 16/09/2016.
 *
 * Inspired by Sahar Chaillou
 *
 * This is used to get some statistics information such as number of apps, platforms, ...
 *
 */

@Path("/stats")
@Api("/stats")
public class HesperidesStatsResource {

    private final Applications applicationsAggregate;
    private final Modules modulesAggregate;
    private final TemplatePackages templatePackagesAggregate;

    public HesperidesStatsResource (final Applications applicationsAggregate, final Modules modulesAggregate, final TemplatePackages
            templatePackagesAggregate){
        this.applicationsAggregate = applicationsAggregate;
        this.modulesAggregate = modulesAggregate;
        this.templatePackagesAggregate = templatePackagesAggregate;
    }

    /**
     * Function used for extract distinct values from collection.
     *
     * @param keyExtractor
     * @param <T>
     * @return the filtered collection
     */
    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @GET
    @Timed
    @ApiOperation("Get some statics data about Hesperides")
    @Produces(MediaType.APPLICATION_JSON)
    public Stats getStats (){
        final Collection<PlatformData> platformList = applicationsAggregate.getAllPlatforms();

        final int ptfCount = platformList.stream().collect(Collectors.toList()).size();
        final int appCount = platformList.stream().filter(distinctByKey(app -> app.getApplicationName())).collect(Collectors.toList()).size();

        final Collection<Module> listModules = modulesAggregate.getAllModules();
        int nbModules = 0;

        if (listModules != null) {
            nbModules = listModules.size();
        }

        final int tplCount = templatePackagesAggregate.getAllTemplates().size();

        return new Stats(appCount, ptfCount, nbModules, tplCount);
    }

    /**
     *  Internal class
     *  This is an internal class for holding statistics
     */
    @JsonSnakeCase
    public static class Stats {

        private final int numberOfApplications;
        private final int numberOfPlatforms;
        private final int numberOfModules;
        private final int numberOfTechnos;

        public Stats(final int numberOfApplications, final int numberOfPlatforms, final int numberOfModules, final int numberOfTechnos) {
            this.numberOfPlatforms = numberOfPlatforms;
            this.numberOfApplications = numberOfApplications;
            this.numberOfModules = numberOfModules;
            this.numberOfTechnos = numberOfTechnos;
        }

        public int getNumberOfPlatforms() {
            return numberOfPlatforms;
        }

        public int getNumberOfApplications() {
            return numberOfApplications;
        }

        public int getNumberOfModules() {
            return numberOfModules;
        }

        public int getNumberOfTechnos() {
            return numberOfTechnos;
        }
    }
}
