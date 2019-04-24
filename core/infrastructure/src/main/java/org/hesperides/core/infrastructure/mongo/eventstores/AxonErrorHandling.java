package org.hesperides.core.infrastructure.mongo.eventstores;

import org.axonframework.config.EventHandlingConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AxonErrorHandling {

    @Autowired
    private EventHandlingConfiguration eventHandlingConfiguration;

    @PostConstruct
    public void registerErrorHandling() {
        // On s'assure que les exceptions levées dans les bus Axon sont toujours remontées
        eventHandlingConfiguration.configureListenerInvocationErrorHandler(configuration -> (exception, event, listener) -> {
            throw exception;
        });
    }
}
