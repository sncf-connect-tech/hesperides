package org.hesperides.core.infrastructure.mongo.events;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MongoEventRepository extends MongoRepository<EventDocument, String> {
    List<EventDocument> findAllByAggregateIdentifier(String aggregateIdentifier);
    void deleteAllByAggregateIdentifier(String aggregateIdentifier);
}
