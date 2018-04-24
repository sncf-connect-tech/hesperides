package org.hesperides.infrastructure.mongo.modules.commands;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.hesperides.domain.modules.ModuleCreatedEvent;
import org.hesperides.domain.modules.ModuleDeletedEvent;
import org.hesperides.domain.modules.ModuleUpdatedEvent;
import org.hesperides.domain.modules.commands.ModuleCommandsRepository;
import org.hesperides.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.infrastructure.mongo.modules.MongoModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Repository;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public class MongoModuleCommandsRepository implements ModuleCommandsRepository {

    private final MongoModuleRepository mongoModuleRepository;

    @Autowired
    public MongoModuleCommandsRepository(MongoModuleRepository mongoModuleRepository) {
        this.mongoModuleRepository = mongoModuleRepository;
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleCreatedEvent event) {
        ModuleDocument moduleDocument = new ModuleDocument();
        moduleDocument.setName(event.getModule().getKey().getName());
        moduleDocument.setVersion(event.getModule().getKey().getVersion());
        moduleDocument.setVersionType(event.getModule().getKey().getVersionType());
        moduleDocument.setVersionId(event.getModule().getVersionId());
        mongoModuleRepository.save(moduleDocument);
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleUpdatedEvent event) {
        //TODO Utiliser la méthode findByKey et sortir le mapping dans ModuleDocument
        ModuleDocument moduleDocument = new ModuleDocument();
        moduleDocument.setName(event.getModule().getKey().getName());
        moduleDocument.setVersion(event.getModule().getKey().getVersion());
        moduleDocument.setVersionType(event.getModule().getKey().getVersionType());
        moduleDocument = mongoModuleRepository.findOne(Example.of(moduleDocument));
        moduleDocument.setVersionId(event.getModule().getVersionId());
        //TODO update properties (technos) then save to db
        mongoModuleRepository.save(moduleDocument);
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleDeletedEvent event) {
        //TODO Utiliser la méthode findByKey et sortir le mapping dans ModuleDocument
        ModuleDocument moduleDocument = new ModuleDocument();
        moduleDocument.setName(event.getModule().getKey().getName());
        moduleDocument.setVersion(event.getModule().getKey().getVersion());
        moduleDocument.setVersionType(event.getModule().getKey().getVersionType());
        moduleDocument = mongoModuleRepository.findOne(Example.of(moduleDocument));
        mongoModuleRepository.delete(moduleDocument);
    }

}
