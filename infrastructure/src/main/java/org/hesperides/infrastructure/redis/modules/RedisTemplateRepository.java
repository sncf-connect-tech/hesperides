package org.hesperides.infrastructure.redis.modules;

import com.thoughtworks.xstream.XStream;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.TemplateByNameQuery;
import org.hesperides.domain.modules.TemplateCreatedEvent;
import org.hesperides.domain.modules.TemplateDeletedEvent;
import org.hesperides.domain.modules.TemplateUpdatedEvent;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.hesperides.domain.modules.queries.TemplateRepository;
import org.hesperides.domain.modules.queries.TemplateView;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * les templates sont stockés dans Redis.
 */
@Component
@Profile("!local")
public class RedisTemplateRepository implements TemplateRepository {

    private final StringRedisTemplate template;
    private final XStream xStream = new XStream();

    public RedisTemplateRepository(StringRedisTemplate template) {
        this.template = template;
    }

    @QueryHandler
    @Override
    public Optional<TemplateView> queryTemplateByName(TemplateByNameQuery query) {
        String payload = template.opsForValue().get(getKey(query.getModuleKey(), query.getTemplateName()));
        if (payload == null) {
            return Optional.empty();
        }
        xStream.setClassLoader(Thread.currentThread().getContextClassLoader());
        return Optional.of((TemplateView) xStream.fromXML(payload));
    }

    @EventSourcingHandler
    public void on(TemplateCreatedEvent event) {
        String payload = xStream.toXML(event.getTemplate().buildTemplateView());
        template.opsForValue().set(getKey(event.getModuleKey(), event.getTemplate()), payload);
    }

    @EventSourcingHandler
    private void on(TemplateUpdatedEvent event) {
        // ecrase le template existant.
        on(new TemplateCreatedEvent(event.getModuleKey(), event.getTemplate(), event.getUser()));
    }

    @EventSourcingHandler
    private void on(TemplateDeletedEvent event) {
        template.delete(getKey(event.getModuleKey(), event.getTemplateName()));
    }

    private String getKey(Module.Key moduleKey, Template template) {
        return getKey(moduleKey, template.getName());
    }

    private String getKey(Module.Key moduleKey, String name) {
        return moduleKey.toString() + "_" + name;
    }
}
