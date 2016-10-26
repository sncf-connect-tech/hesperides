package com.vsct.dt.hesperides;

import javax.validation.Valid;

/**
 * Created by emeric_martineau on 28/01/2016.
 */
public class HesperidesCacheParameter {
    public static final int NOT_SET = -1;

    /**
     * Maximum size in cache.
     */
    @Valid
    private int maxSize = NOT_SET;

    /**
     * Expire time.
     */
    @Valid
    private String itemExpireAfter = null;

    /**
     * Maximum weight.
     */
    @Valid
    private int weight = NOT_SET;

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(final int maxSize) {
        this.maxSize = maxSize;
    }

    public String getItemExpireAfter() {
        return itemExpireAfter;
    }

    public void setItemExpireAfter(final String itemExpireAfter) {
        this.itemExpireAfter = itemExpireAfter;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(final int weight) {
        this.weight = weight;
    }
}
