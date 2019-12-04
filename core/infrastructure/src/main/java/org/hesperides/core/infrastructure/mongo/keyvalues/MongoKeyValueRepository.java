package org.hesperides.core.infrastructure.mongo.keyvalues;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import static org.hesperides.commons.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public interface MongoKeyValueRepository extends MongoRepository<KeyValueDocument, String> {
}
