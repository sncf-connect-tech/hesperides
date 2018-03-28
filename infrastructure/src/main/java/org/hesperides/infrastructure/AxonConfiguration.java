package org.hesperides.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.EventHandlingConfiguration;
import org.axonframework.config.ProcessingGroup;
import org.hesperides.infrastructure.redis.eventstores.RedisTokenStore;
import org.hesperides.infrastructure.views.TrackedProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Slf4j
@Configuration
public class AxonConfiguration {

    @Autowired
    private EventHandlingConfiguration eventHandlingConfiguration;

    @Autowired
    private RedisTokenStore redisTokenStore;

    @PostConstruct
    public void startTrackingProjections() throws ClassNotFoundException {

        log.debug("Register tracking projections.");

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(TrackedProjection.class));
        for (BeanDefinition bd : scanner.findCandidateComponents("org.hesperides")) {

            log.debug("Registering {}", bd.getBeanClassName());

            Class<?> aClass = Class.forName(bd.getBeanClassName());
            ProcessingGroup processingGroup = aClass.getAnnotation(ProcessingGroup.class);
            String name = Optional.ofNullable(processingGroup).map(ProcessingGroup::value).orElse(aClass.getPackage().getName());
            if (aClass.getAnnotation(TrackedProjection.class).useRedis()) {
                eventHandlingConfiguration.registerTokenStore(name, configuration -> redisTokenStore);
            }
            eventHandlingConfiguration.registerTrackingProcessor(name);
        }
    }


}
