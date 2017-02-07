package com.vsct.dt.hesperides.templating;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.dropwizard.jackson.JsonSnakeCase;

/**
 * Created by stephane_fret on 07/02/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
public class TemplateFeedback {
}
