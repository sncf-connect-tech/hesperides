package org.hesperides.infrastructure.modules.local;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.events.ModuleCreatedEvent;
import org.hesperides.domain.modules.events.TemplateCreatedEvent;
import org.hesperides.domain.modules.events.TemplateDeletedEvent;
import org.hesperides.domain.modules.events.TemplateUpdatedEvent;
import org.hesperides.domain.modules.queries.*;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Profile("local")
public class LocalModuleRepository implements ModulesQueries {

    private static final Map<Module.Key, ModuleView> MODULE_MAP = Maps.newHashMap();
    private static final Map<Pair<Module.Key, String>, TemplateView> TEMPLATE_VIEW_MAP = Maps.newHashMap();
    private static final Map<Pair<Module.Key, String>, TemplateContent> TEMPLATE_CONTENT_MAP = Maps.newHashMap();

    @EventSourcingHandler
    private void on(ModuleCreatedEvent event) {
        MODULE_MAP.put(event.getModuleKey(),
                new ModuleView(
                        event.getModuleKey().getName(),
                        event.getModuleKey().getVersion(),
                        event.getModuleKey().getVersionType() == Module.Type.workingcopy,
                        1
                )
        );
    }

    @EventSourcingHandler
    private void on(TemplateCreatedEvent event) {
        Pair<Module.Key, String> key = Pair.of(event.getModuleKey(), event.getTemplate().getName());
        TEMPLATE_VIEW_MAP.put(key, new TemplateView(
                event.getTemplate().getName(),
                "modules#" + event.getModuleKey().getName() + "#" + event.getModuleKey().getVersion()
                        + "#" + event.getTemplate().getName() + "#" + event.getModuleKey().getVersionType().name().toUpperCase(),
                event.getTemplate().getFilename(),
                event.getTemplate().getLocation()
        ));
        TEMPLATE_CONTENT_MAP.put(key, new TemplateContent(event.getTemplate().getContent()));
    }

    @EventSourcingHandler
    private void on(TemplateUpdatedEvent event) {
        Pair<Module.Key, String> key = Pair.of(event.getModuleKey(), event.getTemplate().getName());
        TEMPLATE_VIEW_MAP.put(key, new TemplateView(
                event.getTemplate().getName(),
                "modules#" + event.getModuleKey().getName() + "#" + event.getModuleKey().getVersion()
                        + "#" + event.getTemplate().getName() + "#" + event.getModuleKey().getVersionType().name().toUpperCase(),
                event.getTemplate().getFilename(),
                event.getTemplate().getLocation()
        ));
        TEMPLATE_CONTENT_MAP.put(key, new TemplateContent(event.getTemplate().getContent()));
    }

    @EventSourcingHandler
    private void on(TemplateDeletedEvent event) {
        Pair<Module.Key, String> key = Pair.of(event.getModuleKey(), event.getTemplateName());
        TEMPLATE_VIEW_MAP.remove(key);
        TEMPLATE_CONTENT_MAP.remove(key);
    }

    @QueryHandler
    public Optional<ModuleView> query(ModuleByIdQuery query) {
        return Optional.ofNullable(MODULE_MAP.get(query.getKey()));
    }

    @QueryHandler
    public List<String> queryAllModuleNames(ModulesNamesQuery query) {
        return ImmutableList.copyOf(MODULE_MAP.keySet()).stream().map(Module.Key::getName).collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public Optional<TemplateView> queryTemplateByName(TemplateByNameQuery query) {
        return Optional.ofNullable(TEMPLATE_VIEW_MAP.get(Pair.of(query.getModuleKey(), query.getTemplateName())));
    }
}
