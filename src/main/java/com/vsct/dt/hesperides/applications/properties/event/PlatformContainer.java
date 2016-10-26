package com.vsct.dt.hesperides.applications.properties.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by emeric_martineau on 18/01/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformContainer {
    /**
     * Current platform.
     */
    @JsonProperty("platform")
    private PlatformData platform;

    /**
     * Current properties.
     */
    @JsonProperty("properties")
    private Map<String, PropertiesData> properties = new ConcurrentHashMap<>();

    @JsonCreator
    public PlatformContainer(@JsonProperty("platform") final PlatformData platform,
                             @JsonProperty("properties") final Map<String, PropertiesData> properties) {
        this.platform = platform;

        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    public PlatformContainer() {
        /// Nothing
    }

    /**
     * Return platform.
     *
     * @return platform.
     */
    public PlatformData getPlatform() {
        return this.platform;
    }

    public Map<String, PropertiesData> getProperties() {
        return this.properties;
    }

    public void setPlatform(final PlatformData platform) {
        this.platform = platform;
    }

    public void addProperties(final Map<String, PropertiesData> properties) {
        this.properties.putAll(properties);
    }
}
