package org.hesperides.core.infrastructure.mongo.events;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import static org.hesperides.core.infrastructure.mongo.Collections.DOMAINEVENTS;

@Data
@Document(collection = DOMAINEVENTS)
public class EventDocument {
    String aggregateIdentifier;
    String type;
    int sequenceNumber;
    String serializedPayload;
    String timestamp;
    String payloadType;
    String serializedMetadata;
    String eventIdentifier;
}
