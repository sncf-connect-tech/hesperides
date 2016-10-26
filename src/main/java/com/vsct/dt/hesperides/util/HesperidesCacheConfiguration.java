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
