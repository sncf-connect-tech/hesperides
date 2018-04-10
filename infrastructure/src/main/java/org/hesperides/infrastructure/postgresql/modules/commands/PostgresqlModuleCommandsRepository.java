package org.hesperides.infrastructure.postgresql.modules.commands;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.hesperides.domain.modules.ModuleCreatedEvent;
import org.hesperides.domain.modules.ModuleDeletedEvent;
import org.hesperides.domain.modules.ModuleUpdatedEvent;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.commands.ModuleCommandsRepository;
import org.hesperides.infrastructure.postgresql.modules.ModuleEntity;
import org.hesperides.infrastructure.postgresql.modules.PostgresqlModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("postgresql")
@Component
public class PostgresqlModuleCommandsRepository implements ModuleCommandsRepository {

    private final PostgresqlModuleRepository postgresqlModuleRepository;

    @Autowired
    public PostgresqlModuleCommandsRepository(PostgresqlModuleRepository postgresqlModuleRepository) {
        this.postgresqlModuleRepository = postgresqlModuleRepository;
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleCreatedEvent event) {
        Module module = event.getModule();
        ModuleEntity.ModuleEntityId id = new ModuleEntity.ModuleEntityId(
                module.getKey().getName(),
                module.getKey().getVersion(),
                module.getKey().getVersionType()
        );
        postgresqlModuleRepository.save(
                new ModuleEntity(
                        id,
                        module.getVersionId()
                )
        );
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleUpdatedEvent event) {
        Module module = event.getModule();
        ModuleEntity.ModuleEntityId id = new ModuleEntity.ModuleEntityId(
                module.getKey().getName(),
                module.getKey().getVersion(),
                module.getKey().getVersionType()
        );
        postgresqlModuleRepository.save(
                new ModuleEntity(
                        id,
                        module.getVersionId()
                )
        );
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleDeletedEvent event) {
        Module module = event.getModule();
        ModuleEntity.ModuleEntityId id = new ModuleEntity.ModuleEntityId(
                module.getKey().getName(),
                module.getKey().getVersion(),
                module.getKey().getVersionType()
        );
        postgresqlModuleRepository.delete(id);
    }

}
