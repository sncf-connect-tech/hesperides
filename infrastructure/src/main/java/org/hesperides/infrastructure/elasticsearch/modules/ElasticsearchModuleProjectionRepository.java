package org.hesperides.infrastructure.elasticsearch.modules;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.queries.AbstractPropertyView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.domain.framework.Profiles.ELASTICSEARCH;

@Slf4j
@Profile(ELASTICSEARCH)
@Component
public class ElasticsearchModuleProjectionRepository implements ModuleProjectionRepository {
    private ElasticsearchModuleRepository elasticsearchModuleRepository;

    @Autowired
    public ElasticsearchModuleProjectionRepository(ElasticsearchModuleRepository elasticsearchModuleRepository) {
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
    public void on(ModuleTechnosUpdatedEvent event) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleDeletedEvent event) {
        TemplateContainer.Key moduleKey = event.getModuleKey();
        ModuleDocument moduleDocument = elasticsearchModuleRepository.findOneByNameAndVersionAndVersionType(
                moduleKey.getName(),
                moduleKey.getVersion(),
                moduleKey.getVersionType());
        elasticsearchModuleRepository.delete(moduleDocument);
    }

    @QueryHandler
    @Override
    public Optional<ModuleView> query(GetModuleByKeyQuery query) {
        Optional<ModuleView> optionalModuleView = Optional.empty();
        ModuleDocument moduleDocument = elasticsearchModuleRepository.findOneByNameAndVersionAndVersionType(
                query.getModuleKey().getName(),
                query.getModuleKey().getVersion(),
                query.getModuleKey().getVersionType());

        if (moduleDocument != null) {
            optionalModuleView = Optional.of(moduleDocument.toModuleView());
        }
        return optionalModuleView;
    }

    @QueryHandler
    @Override
    public List<String> query(GetModulesNamesQuery query) {
        return elasticsearchModuleRepository.findAll()
                .getContent()
                .stream()
                .map(ModuleDocument::getName)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> query(GetModuleVersionTypesQuery query) {
        ModuleDocument moduleDocument = new ModuleDocument();
        moduleDocument.setName(query.getModuleName());
        moduleDocument.setVersion(query.getModuleVersion());
        return elasticsearchModuleRepository.findAllByNameAndVersion(query.getModuleName(), query.getModuleVersion())
                .stream()
                .map(ModuleDocument::getVersionType)
                .map(TemplateContainer.VersionType::toString)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> query(GetModuleVersionsQuery query) {
        ModuleDocument moduleDocument = new ModuleDocument();
        moduleDocument.setName(query.getModuleName());
        return elasticsearchModuleRepository.findAllByName(query.getModuleName())
                .stream()
                .map(ModuleDocument::getVersion)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public Boolean query(ModuleAlreadyExistsQuery query) {
        ModuleDocument moduleDocument = elasticsearchModuleRepository.findOneByNameAndVersionAndVersionType(
                query.getModuleKey().getName(),
                query.getModuleKey().getVersion(),
                query.getModuleKey().getVersionType());
        return moduleDocument != null;
    }

    @QueryHandler
    @Override
    public List<ModuleView> query(SearchModulesQuery query) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<AbstractPropertyView> query(GetModulePropertiesQuery query) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
