package org.hesperides.core.infrastructure.mongo.keyvalues;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.keyvalues.*;
import org.hesperides.core.domain.keyvalues.queries.views.KeyValueView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

    @Override
    public void onKeyValueCreatedEvent(KeyValueCreatedEvent event) {
        KeyValueDocument keyValueDocument = new KeyValueDocument(event.getId(), event.getKeyValue());
        keyValueRepository.save(keyValueDocument);
    }

    @Override
    public void onKeyValueUpdatedEvent(KeyValueUpdatedEvent event) {
        KeyValueDocument keyValueDocument = new KeyValueDocument(event.getId(), event.getKeyValue());
        keyValueRepository.save(keyValueDocument);
    }

    @Override
    public void onKeyValueDeletedEvent(KeyValueDeletedEvent event) {
        keyValueRepository.deleteById(event.getId());
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    public Boolean onKeyValueExistsQuery(KeyValueExistsQuery query) {
        return keyValueRepository.existsById(query.getId());
    }

    @QueryHandler
    @Override
    public Optional<KeyValueView> onGetKeyValueQuery(GetKeyValueQuery query) {
        return keyValueRepository.findById(query.getId())
                .map(KeyValueDocument::toKeyValueView);
    }
}
