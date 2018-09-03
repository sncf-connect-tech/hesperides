package org.hesperides.core.infrastructure.mongo.modules;

import org.hesperides.core.infrastructure.mongo.templatecontainers.KeyDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public interface MongoModuleRepository extends MongoRepository<ModuleDocument, String> {

    ModuleDocument findByKey(KeyDocument key);

    Optional<ModuleDocument> findOptionalByKey(KeyDocument key);

    List<ModuleDocument> findByKeyNameAndKeyVersion(String name, String version);

    List<ModuleDocument> findByKeyName(String name);

    void deleteByKey(KeyDocument key);

    Optional<ModuleDocument> findOptionalByKeyAndTemplatesName(KeyDocument key, String templateName);

    List<ModuleDocument> findAllByKeyNameLikeAndAndKeyVersionLike(String name, String version, Pageable pageable);

    @Query(value = "{'technos.$id': ?0}")
    List<ModuleDocument> findAllByTechnosKey(KeyDocument technoKey);
}
