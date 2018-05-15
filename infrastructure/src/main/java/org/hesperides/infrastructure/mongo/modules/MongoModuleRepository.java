package org.hesperides.infrastructure.mongo.modules;

import org.hesperides.infrastructure.mongo.templatecontainer.KeyDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public interface MongoModuleRepository extends MongoRepository<ModuleDocument, String> {

    ModuleDocument findByKey(KeyDocument key);

    List<ModuleDocument> findByKeyNameAndKeyVersion(String name, String version);

    List<ModuleDocument> findByKeyName(String name);

    void deleteByKey(KeyDocument key);

    ModuleDocument findByKeyAndTemplatesName(KeyDocument key, String templateName);

    List<ModuleDocument> findAllByKeyNameLikeAndAndKeyVersionLike(String name, String version, Pageable pageable);
}
