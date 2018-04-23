package org.hesperides.infrastructure.elasticsearch.modules.queries;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleQueriesRepository;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.infrastructure.elasticsearch.modules.ElasticsearchModuleRepository;
import org.hesperides.infrastructure.elasticsearch.modules.ModuleDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Profile("elasticsearch")
@Component
public class ElasticsearchModuleQueriesRepository implements ModuleQueriesRepository {

    private final ElasticsearchModuleRepository elasticsearchModuleRepository;

    @Autowired
    public ElasticsearchModuleQueriesRepository(ElasticsearchModuleRepository elasticsearchModuleRepository) {
        this.elasticsearchModuleRepository = elasticsearchModuleRepository;
    }

    @QueryHandler
    @Override
    public Optional<ModuleView> query(ModuleByIdQuery query) {
        ModuleDocument moduleDocument = elasticsearchModuleRepository.findOneByNameAndVersionAndVersionType(
                query.getModuleKey().getName(),
                query.getModuleKey().getVersion(),
                query.getModuleKey().getVersionType());
        if (moduleDocument == null) {
            return Optional.empty();
        }
        return Optional.of(
                new ModuleView(
                        moduleDocument.getName(),
                        moduleDocument.getVersion(),
                        moduleDocument.getVersionType() == Module.Type.workingcopy,
                        moduleDocument.getVersionId()
                )
        );
    }

    @QueryHandler
    @Override
    public List<String> queryAllModuleNames(ModulesNamesQuery query) {
        return elasticsearchModuleRepository.findAll()
                .getContent()
                .stream()
                .map(ModuleDocument::getName)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> queryModuleTypes(ModuleTypesQuery query) {
        ModuleDocument moduleDocument = new ModuleDocument();
        moduleDocument.setName(query.getModuleName());
        moduleDocument.setVersion(query.getModuleVersion());
        return elasticsearchModuleRepository.findAllByNameAndVersion(query.getModuleName(), query.getModuleVersion())
                .stream()
                .map(ModuleDocument::getVersionType)
                .map(Module.Type::toString)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> queryModuleVersions(ModuleVersionsQuery query) {
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
}
