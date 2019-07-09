package org.hesperides.core.infrastructure.monitoring;

import io.sentry.Sentry;
import io.sentry.event.helper.BasicRemoteAddressResolver;
import io.sentry.event.helper.RemoteAddressResolver;
import io.sentry.event.interfaces.ExceptionInterface;
import io.sentry.event.interfaces.UserInterface;
import io.sentry.servlet.SentryServletRequestListener;
import io.sentry.spring.SentryExceptionResolver;
import io.sentry.spring.SentryServletContextInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.springframework.util.StringUtils.split;

@Configuration
@Slf4j
public class SentryConfiguration {

    private final RemoteAddressResolver remoteAddressResolver = new BasicRemoteAddressResolver();

    @Value("#{'${sentry.ignored-exceptions}'.split(',')}")
    private String[] ignoredExceptions;

    @Bean
    @ConditionalOnProperty("SENTRY_DSN") // only if environment variable exists
    public HandlerExceptionResolver sentryExceptionResolver() {
        // Recipe FROM: https://github.com/getsentry/sentry-java/issues/575
        Sentry.getStoredClient().addShouldSendEventCallback(event ->
                event.getSentryInterfaces().values().stream()
                        .filter(ExceptionInterface.class::isInstance)
                        .map(ExceptionInterface.class::cast)
                        .map(ExceptionInterface::getExceptions)
                        .flatMap(Collection::stream)
                        .noneMatch(sentryException ->
                                Arrays.stream(ignoredExceptions).anyMatch(ignoredException -> sentryException.getExceptionClassName().equals(ignoredException))
                        ));
        Sentry.getStoredClient().addBuilderHelper(eventBuilder -> {
            HttpServletRequest request = SentryServletRequestListener.getServletRequest();
            if (request == null) {
                return;
            }
            eventBuilder.withTag("method", request.getMethod());
            eventBuilder.withTag("application", extractApplication(request.getRequestURI()));
            eventBuilder.withTag("uri", defaultString(getMatchingPattern(request)));
            eventBuilder.withTag("query", defaultString(request.getQueryString()));
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            eventBuilder.withSentryInterface(new UserInterface(null, authentication.getName(),
                    remoteAddressResolver.getRemoteAddress(request), null), true);
        });
        log.info("Creating a SentryExceptionResolver as HandlerExceptionResolver - Ignored exceptions: {}", ignoredExceptions);
        return new SentryExceptionResolver();
    }

    @Bean
    @ConditionalOnProperty("SENTRY_DSN") // only if environment variable exists
    public static SentryServletContextInitializer sentryServletContextInitializer() {
        // Needed for SentryServletRequestListener.getServletRequest() to work
        return new SentryServletContextInitializer();
    }

    private static String extractApplication(String requestURI) {
        requestURI = requestURI.replace("/rest/", "");
        requestURI = requestURI.replace("files/", "");
        if (requestURI.startsWith("applications/")) {
            requestURI = requestURI.replace("applications/", "");
            String[] path = split(requestURI, "/");
            return (path != null && path.length > 0) ? path[0] : "";
        }
        return "";
    }

    // Copy of WebMvcTags.getMatchingPattern
    private static final String DATA_REST_PATH_PATTERN_ATTRIBUTE = "org.springframework.data.rest.webmvc.RepositoryRestHandlerMapping.EFFECTIVE_REPOSITORY_RESOURCE_LOOKUP_PATH";
    private static String getMatchingPattern(HttpServletRequest request) {
        PathPattern dataRestPathPattern = (PathPattern) request
                .getAttribute(DATA_REST_PATH_PATTERN_ATTRIBUTE);
        if (dataRestPathPattern != null) {
            return dataRestPathPattern.getPatternString();
        }
        return (String) request
                .getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    }
}