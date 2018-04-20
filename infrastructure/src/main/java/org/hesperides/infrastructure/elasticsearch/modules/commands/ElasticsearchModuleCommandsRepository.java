package org.hesperides.infrastructure.elasticsearch.modules.commands;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.hesperides.domain.modules.ModuleCreatedEvent;
import org.hesperides.domain.modules.ModuleDeletedEvent;
import org.hesperides.domain.modules.ModuleUpdatedEvent;
import org.hesperides.domain.modules.commands.ModuleCommandsRepository;
import org.hesperides.infrastructure.elasticsearch.modules.ElasticsearchModuleRepository;
import org.hesperides.infrastructure.elasticsearch.modules.ModuleDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("elasticsearch")
@Component
public class ElasticsearchModuleCommandsRepository implements ModuleCommandsRepository {
    private ElasticsearchModuleRepository elasticsearchModuleRepository;

    @Autowired
    public ElasticsearchModuleCommandsRepository(ElasticsearchModuleRepository elasticsearchModuleRepository) {
        this.elasticsearchModuleRepository = elasticsearchModuleRepository;
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleCreatedEvent event) {
        ModuleDocument moduleDocument = new ModuleDocument();
        moduleDocument.setName(event.getModule().getKey().getName());
        moduleDocument.setVersion(event.getModule().getKey().getVersion());
        moduleDocument.setVersionType(event.getModule().getKey().getVersionType());
        moduleDocument.setVersionId(event.getModule().getVersionId());
        elasticsearchModuleRepository.save(moduleDocument);
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleUpdatedEvent event) {
        ModuleDocument moduleDocument = new ModuleDocument();
        moduleDocument.setName(event.getModule().getKey().getName());
        moduleDocument.setVersion(event.getModule().getKey().getVersion());
        moduleDocument.setVersionType(event.getModule().getKey().getVersionType());
        moduleDocument.setVersionId(event.getModule().getVersionId());
        moduleDocument = elasticsearchModuleRepository.findOneByNameAndVersionAndVersionTypeAndVersionId(
                event.getModule().getKey().getName(),
                event.getModule().getKey().getVersion(),
                event.getModule().getKey().getVersionType(),
                event.getModule().getVersionId());
        //TODO update properties (technos) then save to db
        elasticsearchModuleRepository.save(moduleDocument);
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleDeletedEvent event) {
        ModuleDocument moduleDocument = elasticsearchModuleRepository.findOneByNameAndVersionAndVersionTypeAndVersionId(
                event.getModule().getKey().getName(),
                event.getModule().getKey().getVersion(),
                event.getModule().getKey().getVersionType(),
                event.getModule().getVersionId());
        elasticsearchModuleRepository.delete(moduleDocument);
    }
}
