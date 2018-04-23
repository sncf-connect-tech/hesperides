package org.hesperides.infrastructure.jpa.modules.commands;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.hesperides.domain.modules.ModuleCreatedEvent;
import org.hesperides.domain.modules.ModuleDeletedEvent;
import org.hesperides.domain.modules.ModuleUpdatedEvent;
import org.hesperides.domain.modules.commands.ModuleCommandsRepository;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.infrastructure.jpa.modules.JpaModuleRepository;
import org.hesperides.infrastructure.jpa.modules.ModuleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("jpa")
@Component
public class JpaModuleCommandsRepository implements ModuleCommandsRepository {

    private final JpaModuleRepository jpaModuleRepository;

    @Autowired
    public JpaModuleCommandsRepository(JpaModuleRepository jpaModuleRepository) {
        this.jpaModuleRepository = jpaModuleRepository;
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
        jpaModuleRepository.save(
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
        jpaModuleRepository.save(
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
        jpaModuleRepository.delete(id);
    }

}
