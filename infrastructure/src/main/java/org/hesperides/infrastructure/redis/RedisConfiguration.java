package org.hesperides.infrastructure.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("redis")
@Getter
@Setter
public class RedisConfiguration {

    /**
     * si true: alors on relie tous les events depuis le début pour recréer les views.
     */
    boolean shouldResetEventTrackingTokenOnStartUp = false;
}
