package org.hesperides.infrastructure.mongo.modules.commands;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.hesperides.domain.modules.ModuleCreatedEvent;
import org.hesperides.domain.modules.ModuleDeletedEvent;
import org.hesperides.domain.modules.ModuleUpdatedEvent;
import org.hesperides.domain.modules.commands.ModuleCommandsRepository;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.infrastructure.mongo.modules.MongoModuleRepository;
import org.hesperides.infrastructure.mongo.technos.TechnoDocument;
import org.hesperides.infrastructure.mongo.technos.queries.MongoTechnoQueriesRepository;
import org.hesperides.infrastructure.mongo.templatecontainer.KeyDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public class MongoModuleCommandsRepository implements ModuleCommandsRepository {

    private final MongoModuleRepository moduleRepository;
    private final MongoTechnoQueriesRepository technoQueriesRepository;

    @Autowired
    public MongoModuleCommandsRepository(MongoModuleRepository moduleRepository, MongoTechnoQueriesRepository technoQueriesRepository) {
        this.moduleRepository = moduleRepository;
        this.technoQueriesRepository = technoQueriesRepository;
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleCreatedEvent event) {
        saveFromDomainInstance(event.getModule());
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleUpdatedEvent event) {
        saveFromDomainInstance(event.getModule());
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleDeletedEvent event) {
        moduleRepository.deleteByKey(KeyDocument.fromDomainInstance(event.getModuleKey()));
    }

    private void saveFromDomainInstance(Module module) {
        List<TechnoDocument> technoDocuments = technoQueriesRepository.getTechnoDocumentsFromDomainInstances(module.getTechnos());
        ModuleDocument moduleDocument = ModuleDocument.fromDomainInstance(module, technoDocuments);
        moduleRepository.save(moduleDocument);
    }
}
