package org.hesperides.core.infrastructure.mongo.events;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoEventRepository extends MongoRepository<EventDocument, String> {
    void deleteAllByAggregateIdentifier(String aggregateIdentifier);
}
