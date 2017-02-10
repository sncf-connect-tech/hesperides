package com.vsct.dt.hesperides.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by stephane_fret on 09/02/2017.
 */
public class FeedbackConfiguration {
    @NotEmpty
    @JsonProperty
    private String imagePathStorage;

    /* Hipchat */
    @NotEmpty
    @JsonProperty
    private String hipchatSubdomain;

    @NotEmpty
    @JsonProperty
    private String hipchatId;

    @NotEmpty
    @JsonProperty
    private String hipchatToken;

    public String getImagePathStorage() {
        return imagePathStorage;
    }

    public void setImagePathStorage(String imagePathStorage) {
        this.imagePathStorage = imagePathStorage;
    }

    public String getHipchatSubdomain() { return hipchatSubdomain; }

    public void setHipchatSubdomain(String hipchatSubdomain) { this.hipchatSubdomain = hipchatSubdomain; }

    public String getHipchatId() { return hipchatId; }

    public void setHipchatId(String hipchatId) { this.hipchatId = hipchatId; }

    public String getHipchatToken() { return hipchatToken; }

    public void setHipchatToken(String hipchatToken) { this.hipchatToken = hipchatToken; }
}
