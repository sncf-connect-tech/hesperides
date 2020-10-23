package org.hesperides.core.infrastructure.mongo.technos;

import org.hesperides.core.infrastructure.mongo.templatecontainers.KeyDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.hesperides.commons.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public interface MongoTechnoRepository extends MongoRepository<TechnoDocument, String> {

    @Query(value = "{ 'key' : ?0 }", fields = "{ '_id' : 1 }")
    Optional<TechnoDocument> findOptionalIdByKey(KeyDocument key);

    Optional<TechnoDocument> findOptionalTechnoByKey(KeyDocument key);

    @Query(value = "{ 'key' : ?0 }", fields = "{ 'properties' : 1 }")
    TechnoDocument findPropertiesByTechnoKey(KeyDocument key);

    @Query(value = "{ 'key' : ?0 }", fields = "{ 'templates' : { $elemMatch : { 'name' : ?1 }}}")
    Optional<TechnoDocument> findTemplateByTechnoKeyAndTemplateName(KeyDocument technoKey, String templateName);

    @Query(value = "{ 'key' : ?0 }", fields = "{ 'templates' : 1 }")
    Optional<TechnoDocument> findTemplatesByTechnoKey(KeyDocument technoKey);

    List<TechnoDocument> findAllByKeyIn(List<KeyDocument> keys);

    @Query(value = "{ 'key.name': { '$regex' : ?0, '$options' : 'i' }, 'key.version': { '$regex' : ?1, '$options' : 'i' } }")
    List<TechnoDocument> findAllByKeyNameLikeAndKeyVersionLike(String name, String version, Pageable pageable);

    boolean existsByKey(KeyDocument keyDocument);

    @Query(value = "{ 'key.name' : ?0, 'key.version' : ?1 }", fields = "{ 'key' : 1 }")
    List<TechnoDocument> findKeysByNameAndVersion(String technoName, String technoVersion);

    @Query(value = "{ 'key.name' : ?0 }", fields = "{ 'key.version' : 1 }")
    List<TechnoDocument> findVersionsByKeyName(String technoName);

    @Query(exists = true, value = "{ 'key' : { $in: ?0 }, 'properties.isPassword' : true }")
    Boolean hasPasswords(List<KeyDocument> technoKeys);

    @Query(value = "{ '_id' : { $in: ?0 } }", fields = "{ 'key' : 1 }")
    List<TechnoDocument> findKeysByIdsIn(List<String> technoIds);

    @Query(value = "{ 'key' : { $in: ?0 }, 'properties.isPassword' : true }", fields = "{ 'key' : 1 }")
    List<TechnoDocument> findTechnosWithPasswordWithin(List<KeyDocument> technoKeys);
}
