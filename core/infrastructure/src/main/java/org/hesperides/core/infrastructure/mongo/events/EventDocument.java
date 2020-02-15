package org.hesperides.core.infrastructure.mongo.events;

import lombok.Data;
import org.hesperides.core.domain.events.queries.EventView;
import org.springframework.data.mongodb.core.mapping.Document;

import static org.hesperides.core.infrastructure.mongo.Collections.DOMAINEVENTS;

@Data
@Document(collection = DOMAINEVENTS)
public class EventDocument {

    private String aggregateIdentifier;
    private String type;
    private int sequenceNumber;
    private String serializedPayload;
    private String timestamp;
    private String payloadType;
    private String serializedMetadata;
    private String eventIdentifier;
}
