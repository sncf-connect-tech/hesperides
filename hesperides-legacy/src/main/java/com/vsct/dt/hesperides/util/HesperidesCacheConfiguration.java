/*
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
 */

package com.vsct.dt.hesperides.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vsct.dt.hesperides.HesperidesCacheParameter;
import com.vsct.dt.hesperides.storage.RetryRedisConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by emeric_martineau on 28/01/2016.
 */
public class HesperidesCacheConfiguration {
    private HesperidesCacheParameter templatePackage = new HesperidesCacheParameter();

    private HesperidesCacheParameter module = new HesperidesCacheParameter();

    private HesperidesCacheParameter platform = new HesperidesCacheParameter();

    private HesperidesCacheParameter platformTimeline = new HesperidesCacheParameter();

    @Valid
    @NotNull
    @JsonProperty
    private RetryRedisConfiguration redisConfiguration;

    private long nbEventBeforePersiste;

    /**
     * When migrate from Hesperides 0.3.x to 0.4.x, need generate cache.
     */
    private boolean generateCaheOnStartup = false;

    public long getNbEventBeforePersiste() {
        return nbEventBeforePersiste;
    }

    public void setNbEventBeforePersiste(long nbEventBeforePersiste) {
        this.nbEventBeforePersiste = nbEventBeforePersiste;
    }

    public HesperidesCacheParameter getPlatformTimeline() {
        return platformTimeline;
    }

    public void setPlatformTimeline(HesperidesCacheParameter platformTimeline) {
        this.platformTimeline = platformTimeline;
    }

    public RetryRedisConfiguration getRedisConfiguration() {
        return redisConfiguration;
    }

    public void setRedisConfiguration(RetryRedisConfiguration redisConfiguration) {
        this.redisConfiguration = redisConfiguration;
    }

    public HesperidesCacheParameter getModule() {
        return module;
    }

    public void setModule(HesperidesCacheParameter module) {
        this.module = module;
    }

    public HesperidesCacheParameter getTemplatePackage() {
        return templatePackage;
    }

    public void setTemplatePackage(HesperidesCacheParameter templatePackage) {
        this.templatePackage = templatePackage;
    }

    public HesperidesCacheParameter getPlatform() {
        return platform;
    }

    public void setPlatform(HesperidesCacheParameter platform) {
        this.platform = platform;
    }

    public boolean isGenerateCaheOnStartup() {
        return generateCaheOnStartup;
    }

    public void setGenerateCaheOnStartup(boolean generateCaheOnStartup) {
        this.generateCaheOnStartup = generateCaheOnStartup;
    }
}
