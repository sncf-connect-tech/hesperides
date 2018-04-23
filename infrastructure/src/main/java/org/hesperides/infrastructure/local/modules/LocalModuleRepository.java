package org.hesperides.infrastructure.local.modules;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.commands.ModuleCommandsRepository;
import org.hesperides.domain.modules.commands.TemplateCommandsRepository;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleQueriesRepository;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.modules.queries.TemplateQueriesRepository;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
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
public class LocalModuleRepository implements ModuleCommandsRepository, ModuleQueriesRepository, TemplateCommandsRepository, TemplateQueriesRepository {

    private final Map<Module.Key, ModuleView> MODULE_MAP = Maps.newHashMap();
    private final Map<Pair<Module.Key, String>, TemplateView> TEMPLATE_VIEW_MAP = Maps.newHashMap();
    private final Map<Pair<Module.Key, String>, TemplateContent> TEMPLATE_CONTENT_MAP = Maps.newHashMap();


    @Override
    @EventSourcingHandler
    public void on(ModuleCreatedEvent event) {
        log.debug("handling event {}", event);
        MODULE_MAP.put(event.getModule().getKey(),
                new ModuleView(
                        event.getModule().getKey().getName(),
                        event.getModule().getKey().getVersion(),
                        event.getModule().getKey().getVersionType() == Module.Type.workingcopy,
                        event.getModule().getVersionId()
                )
        );
    }

    @Override
    @EventSourcingHandler
    public void on(ModuleUpdatedEvent event) {
        log.debug("handling event {}", event);
        MODULE_MAP.put(event.getModule().getKey(),
                new ModuleView(
                        event.getModule().getKey().getName(),
                        event.getModule().getKey().getVersion(),
                        event.getModule().getKey().getVersionType() == Module.Type.workingcopy,
                        event.getModule().getVersionId()
                )
        );
    }

    @Override
    @EventSourcingHandler
    public void on(ModuleDeletedEvent event) {
        log.debug("handling event {}", event);
        MODULE_MAP.remove(event.getModule().getKey());
    }

    @Override
    @EventSourcingHandler
    public void on(TemplateCreatedEvent event) {
        log.debug("handling event {}", event);
        Template template = event.getTemplate();
        Pair<Module.Key, String> key = Pair.of(event.getModuleKey(), template.getName());
        TEMPLATE_VIEW_MAP.put(key, new TemplateView(
                template.getName(),
                "modules#" + event.getModuleKey().getName() + "#" + event.getModuleKey().getVersion()
                        + "#" + template.getName() + "#" + event.getModuleKey().getVersionType().name().toUpperCase(),
                template.getFilename(),
                template.getLocation(),
                template.getContent(),
                TemplateView.RightsView.fromDomain(template.getRights()),
                template.getVersionId()
        ));
        TEMPLATE_CONTENT_MAP.put(key, new TemplateContent(template.getContent()));
    }

    @Override
    @EventSourcingHandler
    public void on(TemplateUpdatedEvent event) {
        log.debug("handling event {}", event);
        Template template = event.getTemplate();
        Pair<Module.Key, String> key = Pair.of(event.getModuleKey(), template.getName());
        TEMPLATE_VIEW_MAP.put(key, new TemplateView(
                template.getName(),
                "modules#" + event.getModuleKey().getName() + "#" + event.getModuleKey().getVersion()
                        + "#" + template.getName() + "#" + event.getModuleKey().getVersionType().name().toUpperCase(),
                template.getFilename(),
                template.getLocation(),
                template.getContent(),
                TemplateView.RightsView.fromDomain(template.getRights()),
                template.getVersionId()
        ));
        TEMPLATE_CONTENT_MAP.put(key, new TemplateContent(template.getContent()));
    }

    @Override
    @EventSourcingHandler
    public void on(TemplateDeletedEvent event) {
        log.debug("handling event {}", event);
        Pair<Module.Key, String> key = Pair.of(event.getModuleKey(), event.getTemplateName());
        TEMPLATE_VIEW_MAP.remove(key);
        TEMPLATE_CONTENT_MAP.remove(key);
    }

    @Override
    @QueryHandler
    public Boolean query(ModuleAlreadyExistsQuery query) {
        return query(new ModuleByIdQuery(query.getModuleKey())).isPresent();
    }

    @Override
    @QueryHandler
    public Optional<ModuleView> query(ModuleByIdQuery query) {
        return Optional.ofNullable(MODULE_MAP.get(query.getModuleKey()));
    }

    @Override
    @QueryHandler
    public List<String> queryAllModuleNames(ModulesNamesQuery query) {
        return ImmutableList.copyOf(MODULE_MAP.keySet()).stream().map(Module.Key::getName).collect(Collectors.toList());
    }

    @Override
    @QueryHandler
    public List<String> queryModuleTypes(ModuleTypesQuery query) {
        return ImmutableList.copyOf(MODULE_MAP.values()).stream()
                .filter(module -> module.getName().equalsIgnoreCase(query.getModuleName()))
                .filter(module -> module.getVersion().equalsIgnoreCase(query.getModuleVersion()))
                .map(module -> toModuleTypeView(module.isWorkingCopy()))
                .collect(Collectors.toList());
    }

    @Override
    @QueryHandler
    public List<String> queryModuleVersions(ModuleVersionsQuery query) {
        return ImmutableList.copyOf(MODULE_MAP.values()).stream()
                .filter(module -> module.getName().equalsIgnoreCase(query.getModuleName()))
                .map(ModuleView::getVersion).collect(Collectors.toList());
    }

    @Override
    @QueryHandler
    public Optional<TemplateView> queryTemplateByName(TemplateByNameQuery query) {
        return Optional.ofNullable(TEMPLATE_VIEW_MAP.get(Pair.of(query.getModuleKey(), query.getTemplateName())));
    }

    private String toModuleTypeView(Boolean workingCopy) {
        return workingCopy ? "workingcopy" : "release";
    }
}
