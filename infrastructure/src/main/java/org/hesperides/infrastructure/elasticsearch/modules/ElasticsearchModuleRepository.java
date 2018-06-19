package org.hesperides.infrastructure.elasticsearch.modules;

import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElasticsearchModuleRepository extends ElasticsearchRepository<ModuleDocument, String> {

    Page<ModuleDocument> findAll();

    ModuleDocument findOneByNameAndVersionAndVersionType(String name, String version, TemplateContainer.VersionType versionType);

    ModuleDocument findOneByNameAndVersionAndVersionTypeAndVersionId(String name, String version, TemplateContainer.VersionType versionType, Long versionId);

    List<ModuleDocument> findAllByNameAndVersion(String name, String version);

    List<ModuleDocument> findAllByName(String name);

}
