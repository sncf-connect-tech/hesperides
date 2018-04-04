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
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * les templates sont stockés dans Redis.
 */
@Repository
@Profile("!local")
public class RedisTemplateRepository implements TemplateRepository {

    private static final String TEMPLATE_VIEWS = "a_template_views";

    /**
     * Attention ! Ne pas confondre les deux notions de template :
     * - StringRedisTemplate est un objet qui permet d'accéder aux données Redis
     * - RedisTemplateRepository représente un dépôt Redis pour les templates Hespérides
     * Cela prête à confusion...
     */
    private final StringRedisTemplate redisTemplate;
    private final XStream xStream = new XStream();

    public RedisTemplateRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @QueryHandler
    @Override
    public Optional<TemplateView> queryTemplateByName(TemplateByNameQuery query) {
        String payload = (String) redisTemplate.opsForHash().get(TEMPLATE_VIEWS, getKey(query.getModuleKey(), query.getTemplateName()));
        if (payload == null) {
            return Optional.empty();
        }
        xStream.setClassLoader(Thread.currentThread().getContextClassLoader());
        return Optional.of((TemplateView) xStream.fromXML(payload));
    }

    @EventSourcingHandler
    public void on(TemplateCreatedEvent event) {
        // On persiste l'état du template sous forme de vue
        String payload = xStream.toXML(event.getTemplate().buildTemplateView());
        redisTemplate.opsForHash().put(TEMPLATE_VIEWS, getKey(event.getModuleKey(), event.getTemplate()), payload);
    }

    @EventSourcingHandler
    private void on(TemplateUpdatedEvent event) {
        // ecrase le redisTemplate existant.
        on(new TemplateCreatedEvent(event.getModuleKey(), event.getTemplate(), event.getUser()));
    }

    @EventSourcingHandler
    private void on(TemplateDeletedEvent event) {
        redisTemplate.opsForHash().delete(TEMPLATE_VIEWS, getKey(event.getModuleKey(), event.getTemplateName()));
    }

    private String getKey(Module.Key moduleKey, Template template) {
        return getKey(moduleKey, template.getName());
    }

    private String getKey(Module.Key moduleKey, String name) {
        return "template_view_" + moduleKey.toString() + "_" + name;
    }
}
