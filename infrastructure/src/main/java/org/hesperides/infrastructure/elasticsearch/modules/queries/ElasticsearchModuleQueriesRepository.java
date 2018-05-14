package org.hesperides.infrastructure.elasticsearch.modules.queries;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.queries.ModuleQueriesRepository;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.elasticsearch.modules.ElasticsearchModuleRepository;
import org.hesperides.infrastructure.elasticsearch.modules.ModuleDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.domain.Profiles.ELASTICSEARCH;

@Slf4j
@Profile(ELASTICSEARCH)
@Component
public class ElasticsearchModuleQueriesRepository implements ModuleQueriesRepository {

    private final ElasticsearchModuleRepository elasticsearchModuleRepository;

    @Autowired
    public ElasticsearchModuleQueriesRepository(ElasticsearchModuleRepository elasticsearchModuleRepository) {
        this.elasticsearchModuleRepository = elasticsearchModuleRepository;
    }

    @QueryHandler
    @Override
    public Optional<ModuleView> query(GetModuleByKeyQuery query) {
        Optional<ModuleView> result = Optional.empty();
        ModuleDocument moduleDocument = elasticsearchModuleRepository.findOneByNameAndVersionAndVersionType(
                query.getModuleKey().getName(),
                query.getModuleKey().getVersion(),
                query.getModuleKey().getVersionType());

        if (moduleDocument != null) {
            result = Optional.of(moduleDocument.toModuleView());
        }
        return result;
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
}
