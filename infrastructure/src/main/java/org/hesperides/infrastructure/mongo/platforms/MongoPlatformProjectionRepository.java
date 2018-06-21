package org.hesperides.infrastructure.mongo.platforms;

import org.hesperides.domain.platforms.PlatformProjectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import static org.hesperides.domain.framework.Profiles.FAKE_MONGO;
import static org.hesperides.domain.framework.Profiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoPlatformProjectionRepository implements PlatformProjectionRepository {

    private final MongoPlatformRepository platformRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoPlatformProjectionRepository(MongoPlatformRepository platformRepository,
                                             MongoTemplate mongoTemplate) {
        this.platformRepository = platformRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /*** EVENT HANDLERS ***/


    /*** QUERY HANDLERS ***/

}
