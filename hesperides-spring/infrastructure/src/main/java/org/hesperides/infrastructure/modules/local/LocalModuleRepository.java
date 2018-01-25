package org.hesperides.infrastructure.modules.local;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.ModuleSearchRepository;
import org.hesperides.domain.modules.ModuleType;
import org.hesperides.domain.modules.commands.Module;
import org.hesperides.domain.modules.events.ModuleCreatedEvent;
import org.hesperides.domain.modules.queries.ModuleByIdQuery;
import org.hesperides.domain.modules.queries.ModuleView;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Profile("local")
public class LocalModuleRepository implements ModuleSearchRepository {

    private static final Map<Module.Key, ModuleView> MODULE_MAP = Maps.newHashMap();

    public List<String> getModulesNames() {
        return ImmutableList.copyOf(MODULE_MAP.keySet()).stream().map(Module.Key::getName).collect(Collectors.toList());
    }

    @EventSourcingHandler
    private void on(Object event, MetaData metaData) {
      log.debug("got event {}", event);
      if (event instanceof ModuleCreatedEvent) {
          ModuleCreatedEvent event1 = (ModuleCreatedEvent) event;
          MODULE_MAP.put(((ModuleCreatedEvent) event).getModuleKey(),
                  new ModuleView(
                          event1.getModuleKey().getName(),
                          event1.getModuleKey().getVersion(),
                          event1.getModuleKey().getVersionType() == ModuleType.workingcopy,
                          1
                          )
                  );
      }
    }

    @QueryHandler
    private ModuleView query(ModuleByIdQuery query) {
        return MODULE_MAP.get(query.getKey());
    }
}
