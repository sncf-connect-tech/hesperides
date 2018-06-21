package org.hesperides.infrastructure.mongo.platforms;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.platforms.GetPlatformByKeyQuery;
import org.hesperides.domain.platforms.PlatformCreatedEvent;
import org.hesperides.domain.platforms.PlatformProjectionRepository;
import org.hesperides.domain.platforms.queries.views.PlatformView;
import org.hesperides.infrastructure.mongo.platforms.documents.PlatformDocument;
import org.hesperides.infrastructure.mongo.platforms.documents.PlatformKeyDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.hesperides.domain.framework.Profiles.FAKE_MONGO;
import static org.hesperides.domain.framework.Profiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoPlatformProjectionRepository implements PlatformProjectionRepository {

    private final MongoPlatformRepository platformRepository;

    @Autowired
    public MongoPlatformProjectionRepository(MongoPlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    @Override
    public void on(PlatformCreatedEvent event) {
        PlatformDocument platformDocument = PlatformDocument.fromDomainInstance(event.getPlatform());
        platformRepository.save(platformDocument);
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    public Optional<PlatformView> query(GetPlatformByKeyQuery query) {
        Optional<PlatformView> platformView = Optional.empty();
        PlatformKeyDocument platformKeyDocument = PlatformKeyDocument.fromDomainInstance(query.getPlatformKey());
        Optional<PlatformDocument> platformDocument = platformRepository.findOptionalByKey(platformKeyDocument);
        if (platformDocument.isPresent()) {
            platformView = Optional.of(platformDocument.get().toPlateformView());
        }
        return platformView;
    }
}
