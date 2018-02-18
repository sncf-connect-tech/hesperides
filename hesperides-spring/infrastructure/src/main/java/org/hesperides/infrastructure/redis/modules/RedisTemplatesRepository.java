package org.hesperides.infrastructure.redis.modules;

import lombok.extern.slf4j.Slf4j;
import org.hesperides.domain.modules.TemplatesRepository;
import org.hesperides.domain.modules.queries.TemplateByNameQuery;
import org.hesperides.domain.modules.queries.TemplateView;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * les templates sont stockés dans Redis.
 */
@Component
@Slf4j
@Profile("!local")
public class RedisTemplatesRepository implements TemplatesRepository {


    @Override
    public Optional<TemplateView> queryTemplateByName(TemplateByNameQuery query) {
        throw new IllegalArgumentException("todo");
    }
}
