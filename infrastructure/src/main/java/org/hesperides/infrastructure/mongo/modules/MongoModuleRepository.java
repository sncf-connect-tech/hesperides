package org.hesperides.infrastructure.mongo.modules;

import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public interface MongoModuleRepository extends MongoRepository<ModuleDocument, String> {

    ModuleDocument findByNameAndVersionAndVersionType(String name, String version, TemplateContainer.Type versionType);

    List<ModuleDocument> findByNameAndVersion(String name, String version);

    List<ModuleDocument> findByName(String name);

    void deleteByNameAndVersionAndVersionType(String name, String version, TemplateContainer.Type versionType);
}
