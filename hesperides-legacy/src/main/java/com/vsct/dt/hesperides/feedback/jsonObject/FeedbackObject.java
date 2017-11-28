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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Created by stephane_fret on 08/02/2017.
 */
public class FeedbackObject {
    private final String url;
    private final String timestamp;
    private final String img;
    private final String note;

    public FeedbackObject(
            @JsonProperty("url") final String url,
            @JsonProperty("timestamp") final String timestamp,
            @JsonProperty("img") final String img,
            @JsonProperty("note") final String note) {
        this.url = url;
        this.timestamp = timestamp;
        this.img = img;
        this.note = note;
    }

    public String getUrl() {
        return url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getImg() {
        return img;
    }

    public String getNote() {
        return note;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, timestamp, img, note);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final FeedbackObject other = (FeedbackObject) obj;
        return Objects.equals(this.url, other.url)
                && Objects.equals(this.timestamp, other.timestamp)
                && Objects.equals(this.img, other.img)
                && Objects.equals(this.note, other.note);
    }

    public String toJsonString() {

        String charReturn = "\n";

        StringBuilder feedbackObject = new StringBuilder();
        feedbackObject.append("    \"feedback\": {").append(charReturn)
                .append("        \"url\": \"").append(url).append("\",").append(charReturn)
                .append("        \"timestamp\": \"").append(timestamp).append("\",").append(charReturn)
                .append("        \"img\": \"").append(img).append("\",").append(charReturn)
                .append("        \"note\": \"").append(note).append("\",").append(charReturn)
                .append("    }");

        return feedbackObject.toString();
    }
}
