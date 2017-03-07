/*
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
 */

package com.vsct.dt.hesperides.feedback;

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
