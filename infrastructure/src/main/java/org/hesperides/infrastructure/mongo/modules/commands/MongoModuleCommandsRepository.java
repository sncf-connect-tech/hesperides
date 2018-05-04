package org.hesperides.infrastructure.mongo.modules.commands;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.hesperides.domain.modules.ModuleCreatedEvent;
import org.hesperides.domain.modules.ModuleDeletedEvent;
import org.hesperides.domain.modules.ModuleUpdatedEvent;
import org.hesperides.domain.modules.commands.ModuleCommandsRepository;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.infrastructure.mongo.modules.MongoModuleRepository;
import org.hesperides.infrastructure.mongo.technos.TechnoDocument;
import org.hesperides.infrastructure.mongo.technos.queries.MongoTechnoQueriesRepository;
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
        List<TechnoDocument> technos = technoQueriesRepository.getTechnoDocumentsFromDomainInstances(event.getModule().getTechnos());
        ModuleDocument module = ModuleDocument.fromDomain(event.getModule(), technos);
        moduleRepository.save(module);
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleUpdatedEvent event) {
        TemplateContainer.Key key = event.getModule().getKey();
        ModuleDocument existingModule = moduleRepository.findByNameAndVersionAndWorkingCopy(key.getName(), key.getVersion(), key.isWorkingCopy());
        List<TechnoDocument> technos = technoQueriesRepository.getTechnoDocumentsFromDomainInstances(event.getModule().getTechnos());
        ModuleDocument updatedModule = ModuleDocument.fromDomain(event.getModule(), technos);
        updatedModule.setId(existingModule.getId());
        moduleRepository.save(updatedModule);
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleDeletedEvent event) {
        TemplateContainer.Key key = event.getModuleKey();
        moduleRepository.deleteByNameAndVersionAndWorkingCopy(key.getName(), key.getVersion(), key.isWorkingCopy());
    }
}
