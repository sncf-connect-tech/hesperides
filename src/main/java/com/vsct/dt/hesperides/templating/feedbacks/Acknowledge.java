package com.vsct.dt.hesperides.templating.feedbacks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Objects;

/**
 * Created by stephane_fret on 07/02/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"status"})
public class Acknowledge {

    @JsonProperty("status")
    private final String  status;

    @JsonCreator
    public Acknowledge(@JsonProperty("status") String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "HesperidesFeedback{" +
                "status='" + status + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Acknowledge other = (Acknowledge) obj;
        return Objects.equals(this.status, other.status);
    }

}
