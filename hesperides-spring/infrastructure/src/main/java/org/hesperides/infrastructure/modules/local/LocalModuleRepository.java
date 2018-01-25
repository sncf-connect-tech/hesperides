package org.hesperides.infrastructure.modules.local;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.ModuleSearchRepository;
import org.hesperides.domain.modules.commands.Module;
import org.hesperides.domain.modules.queries.ModuleByIdQuery;
import org.hesperides.domain.modules.events.ModuleCreatedEvent;
import org.hesperides.domain.modules.queries.ModuleView;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class LocalModuleRepository implements ModuleSearchRepository {

    private static final Map<Module.Key, ModuleView> MODULE_MAP = Maps.newHashMap();

    public List<String> getModulesNames() {
        return ImmutableList.copyOf(MODULE_MAP.keySet()).stream().map(Module.Key::getName).collect(Collectors.toList());
    }

    @EventSourcingHandler
    private void on(Object event) {
      log.debug("got event {}", event);
      if (event instanceof ModuleCreatedEvent) {
          MODULE_MAP.put(((ModuleCreatedEvent) event).getModuleKey(), new ModuleView(((ModuleCreatedEvent) event).getModuleKey()));
      }
    }

    @QueryHandler
    private ModuleView query(ModuleByIdQuery query) {
        return MODULE_MAP.get(query.getKey());
    }
}
