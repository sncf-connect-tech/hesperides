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

package com.vsct.dt.hesperides.feedback.jsonObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jackson.JsonSnakeCase;

/**
 * Created by stephane_fret on 07/02/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
public class FeedbackJson {
    private final FeedbackObject feedback;

    public FeedbackJson(
            @JsonProperty("feedback") final FeedbackObject feedback) {
        this.feedback = feedback;
    }

    public FeedbackObject getFeedback() {
        return feedback;
    }

    public String toJsonString() {

        String charReturn = "\n";

        StringBuilder feedbackTemplate = new StringBuilder();
        feedbackTemplate.append("{").append(charReturn)
                .append(this.feedback.toJsonString()).append(charReturn).append("}");

        return feedbackTemplate.toString();
    }
}
