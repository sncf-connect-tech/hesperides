package org.hesperides.core.infrastructure.mongo.technos;

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
public interface MongoTechnoRepository extends MongoRepository<TechnoDocument, String> {

    @Query(value = "{ 'key' : ?0 }", fields = "{ '_id' : 1 }")
    Optional<TechnoDocument> findOptionalIdByKey(KeyDocument key);

    Optional<TechnoDocument> findOptionalByKey(KeyDocument key);

    TechnoDocument findByKey(KeyDocument key);

    @Query(value = "{ 'key' : ?0 }", fields = "{ 'templates' : { $elemMatch : { 'name' : ?1 }}}")
    Optional<TechnoDocument> findByKeyAndTemplateName(KeyDocument technoKey, String templateName);

    @Query(value = "{ 'key' : ?0 }", fields = "{ 'templates' : 1 }")
    Optional<TechnoDocument> findTemplatesByTechnoKey(KeyDocument technoKey);

    List<TechnoDocument> findAllByKeyIn(List<KeyDocument> keys);

    List<TechnoDocument> findAllByKeyNameLikeAndKeyVersionLike(String name, String version, Pageable pageable);

    boolean existsByKey(KeyDocument keyDocument);
}
