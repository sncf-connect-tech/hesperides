package org.hesperides.infrastructure.mongo.modules;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public interface MongoModuleRepository extends MongoRepository<ModuleDocument, String> {
    //TODO Créer une methode findByKey
}
