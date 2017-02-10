package com.vsct.dt.hesperides.templating.feedbacks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jackson.JsonSnakeCase;

/**
 * Created by stephane_fret on 07/02/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
public class TemplateFeedback {
    private final FeedbackObject feedback;

    public TemplateFeedback(
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
