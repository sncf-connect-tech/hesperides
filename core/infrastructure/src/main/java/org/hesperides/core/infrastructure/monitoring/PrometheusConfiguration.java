package org.hesperides.core.infrastructure.monitoring;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTags;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Configuration
public class PrometheusConfiguration {

    // "Applying TimedAspect makes @Timed usable on any arbitrary method"
    // FROM: https://micrometer.io/docs/concepts
    @Bean
    TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    // Configuration des tags multi-dimensionnels Prometheus
    // Inspiré de org.springframework.boot.actuate.metrics.web.servlet.DefaultWebMvcTagsProvider
    @Bean
    public WebMvcTagsProvider webMvcTagsProvider() {
        return new WebMvcTagsProvider() {
            @Override
            public Iterable<Tag> getTags(HttpServletRequest request, HttpServletResponse response, Object handler, Throwable exception) {
                List<Tag> tags = new ArrayList<>();
                tags.add(WebMvcTags.method(request));
                tags.add(WebMvcTags.uri(request, response));
                tags.add(Tag.of("path", request.getRequestURI()));
                tags.add(Tag.of("query", defaultString(request.getQueryString())));
                tags.add(WebMvcTags.exception(exception));
                tags.add(WebMvcTags.status(response));
                return tags;
            }

            @Override
            public Iterable<Tag> getLongRequestTags(HttpServletRequest request, Object handler) {
                return Tags.of(WebMvcTags.method(request), WebMvcTags.uri(request, null));
            }
        };
    }
}