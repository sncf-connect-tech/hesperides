package org.hesperides.infrastructure.modules.local;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.Module;
import org.hesperides.domain.modules.ModuleType;
import org.hesperides.domain.modules.events.ModuleCreatedEvent;
import org.hesperides.domain.modules.queries.ModuleByIdQuery;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.modules.queries.ModulesNamesQuery;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Profile("local")
public class LocalModuleRepository {

    private static final Map<Module.Key, ModuleView> MODULE_MAP = Maps.newHashMap();

    @EventSourcingHandler
    private void on(ModuleCreatedEvent event) {
      MODULE_MAP.put(event.getModuleKey(),
              new ModuleView(
                      event.getModuleKey().getName(),
                      event.getModuleKey().getVersion(),
                      event.getModuleKey().getVersionType() == ModuleType.workingcopy,
                      1
                      )
              );
    }

    @QueryHandler
    private ModuleView query(ModuleByIdQuery query) {
        return MODULE_MAP.get(query.getKey());
    }

    @QueryHandler
    private List<String> queryAllModuleNames(ModulesNamesQuery query) {
        return ImmutableList.copyOf(MODULE_MAP.keySet()).stream().map(Module.Key::getName).collect(Collectors.toList());
    }
}
