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
        ModuleDocument moduleDocument = new ModuleDocument();
        TemplateContainer.Key key = event.getModule().getKey();
        moduleDocument.setName(key.getName());
        moduleDocument.setVersion(key.getVersion());
        moduleDocument.setVersionType(key.getVersionType());
        moduleDocument.setVersionId(event.getModule().getVersionId());
        //TODO Templates et technos ?
        repository.save(moduleDocument);
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleUpdatedEvent event) {
        TemplateContainer.Key key = event.getModule().getKey();
        ModuleDocument moduleDocument = repository.findByNameAndVersionAndVersionType(key.getName(), key.getVersion(), key.getVersionType());
        moduleDocument.setVersionId(event.getModule().getVersionId());
        //TODO Templates et technos ?
        repository.save(moduleDocument);
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleDeletedEvent event) {
        TemplateContainer.Key key = event.getModule().getKey();
        repository.deleteByNameAndVersionAndVersionType(key.getName(), key.getVersion(), key.getVersionType());
    }
}
