package org.hesperides.core.infrastructure.mongo.keyvalues;

import org.hesperides.core.domain.keyvalues.KeyValueProjectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import static org.hesperides.commons.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoKeyValueProjectionRepository implements KeyValueProjectionRepository {

    private final MongoKeyValueRepository keyValueRepository;

    @Autowired
    public MongoKeyValueProjectionRepository(MongoKeyValueRepository keyValueRepository) {
        this.keyValueRepository = keyValueRepository;
    }

    /*** EVENT HANDLERS ***/

    /*** QUERY HANDLERS ***/
}
