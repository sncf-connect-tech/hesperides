package org.hesperides.infrastructure.mongo.modules;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public interface MongoModuleRepository extends MongoRepository<ModuleDocument, String> {

    ModuleDocument findByNameAndVersionAndWorkingCopy(String name, String version, boolean isWorkingCopy);

    List<ModuleDocument> findByNameAndVersion(String name, String version);

    List<ModuleDocument> findByName(String name);

    void deleteByNameAndVersionAndWorkingCopy(String name, String version, boolean isWorkingCopy);

    ModuleDocument findByNameAndVersionAndWorkingCopyAndTemplatesName(String name, String version, boolean isWorkingCopy, String templateName);
}
