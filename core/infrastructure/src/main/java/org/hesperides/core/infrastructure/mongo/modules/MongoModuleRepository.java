package org.hesperides.core.infrastructure.mongo.modules;

import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
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

    @Query(value = "{ 'key' : ?0 }", fields = "{ '_id' : 1 }")
    Optional<ModuleDocument> findOptionalIdByKey(KeyDocument key);

    Optional<ModuleDocument> findOptionalById(String id);

    Optional<ModuleDocument> findOptionalByKey(KeyDocument key);

    @Query(value = "{ 'key.name' : ?0, 'key.version' : ?1 }", fields = "{ 'key' : 1 }")
    List<ModuleDocument> findKeysByNameAndVersion(String technoName, String technoVersion);

    @Query(value = "{ 'key.name' : ?0 }", fields = "{ 'key.version' : 1 }")
    List<ModuleDocument> findVersionsByKeyName(String technoName);

    @Query(value = "{ 'key' : ?0 }", fields = "{ 'templates' : { $elemMatch : { 'name' : ?1 }}}")
    Optional<ModuleDocument> findByKeyAndTemplateName(KeyDocument moduleKey, String templateName);

    @Query(value = "{ 'key' : ?0 }", fields = "{ 'templates' : 1 }")
    Optional<ModuleDocument> findTemplatesByModuleKey(KeyDocument moduleKey);

    List<ModuleDocument> findAllByKeyNameLikeAndKeyVersionLike(String name, String version, Pageable pageable);

    @Query(value = "{ 'technos.$id' : ?0 }")
    List<ModuleDocument> findAllByTechnoId(String technoId);

    boolean existsByKey(KeyDocument moduleKey);

    @Query(value = "{ 'key' : ?0 }", fields = "{ 'properties' : 1 }")
    Optional<ModuleDocument> findPropertiesByModuleKey(KeyDocument moduleKey);

    @Query(value = "{ 'key' : { $in: ?0 } }", fields = "{ 'key' : 1, 'properties' : 1 }")
    List<ModuleDocument> findPropertiesByKeyIn(List<KeyDocument> modulesKeys);

    List<ModuleDocument> findAllByTechnosId(String technoId);
}
