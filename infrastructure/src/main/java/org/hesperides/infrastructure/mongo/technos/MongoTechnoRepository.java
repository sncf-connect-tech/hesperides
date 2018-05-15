package org.hesperides.infrastructure.mongo.technos;

import org.hesperides.infrastructure.mongo.templatecontainer.KeyDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public interface MongoTechnoRepository extends MongoRepository<TechnoDocument, String> {

    Optional<TechnoDocument> findOptionalByKey(KeyDocument key);

    TechnoDocument findByKey(KeyDocument key);

    TechnoDocument findByKeyAndTemplatesName(KeyDocument key, String templateName);
}
