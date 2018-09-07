package org.hesperides.core.infrastructure.mongo.workshopproperties;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public interface MongoWorkshopPropertyRepository extends MongoRepository<WorkshopPropertyDocument, String> {

    Optional<WorkshopPropertyDocument> findOptionalByKey(String key);

    WorkshopPropertyDocument findByKey(String key);
}
