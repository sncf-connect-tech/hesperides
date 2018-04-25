package org.hesperides.infrastructure.mongo.modules.commands;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.hesperides.domain.modules.ModuleCreatedEvent;
import org.hesperides.domain.modules.ModuleDeletedEvent;
import org.hesperides.domain.modules.ModuleUpdatedEvent;
import org.hesperides.domain.modules.commands.ModuleCommandsRepository;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.infrastructure.mongo.modules.MongoModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public class MongoModuleCommandsRepository implements ModuleCommandsRepository {

    private final MongoModuleRepository repository;

    @Autowired
    public MongoModuleCommandsRepository(MongoModuleRepository repository) {
        this.repository = repository;
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleCreatedEvent event) {
        ModuleDocument module = ModuleDocument.fromDomain(event.getModule());
        repository.save(module);
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleUpdatedEvent event) {
        TemplateContainer.Key key = event.getModule().getKey();
        ModuleDocument existingModule = repository.findByNameAndVersionAndWorkingCopy(key.getName(), key.getVersion(), key.isWorkingCopy());
        ModuleDocument updatedModule = ModuleDocument.fromDomain(event.getModule());
        updatedModule.setId(existingModule.getId());
        repository.save(updatedModule);
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleDeletedEvent event) {
        TemplateContainer.Key key = event.getModuleKey();
        repository.deleteByNameAndVersionAndWorkingCopy(key.getName(), key.getVersion(), key.isWorkingCopy());
    }
}
