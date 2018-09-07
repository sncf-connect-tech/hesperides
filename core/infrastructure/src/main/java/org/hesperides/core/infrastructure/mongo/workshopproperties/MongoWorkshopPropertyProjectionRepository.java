package org.hesperides.core.infrastructure.mongo.workshopproperties;

import org.hesperides.core.domain.workshopproperties.WorkshopPropertyProjectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoWorkshopPropertyProjectionRepository implements WorkshopPropertyProjectionRepository {

    private final MongoWorkshopPropertyRepository workshopPropertyRepository;

    @Autowired
    public MongoWorkshopPropertyProjectionRepository(MongoWorkshopPropertyRepository workshopPropertyRepository) {
        this.workshopPropertyRepository = workshopPropertyRepository;
    }

    /*** EVENT HANDLERS ***/

}
