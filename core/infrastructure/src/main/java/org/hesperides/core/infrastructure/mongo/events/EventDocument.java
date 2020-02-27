package org.hesperides.core.infrastructure.mongo.events;

import com.thoughtworks.xstream.XStream;
import lombok.Data;
import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.security.UserEvent;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

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

    public EventView toEventView() {
        return new EventView(
                payloadType,
                (UserEvent) new XStream().fromXML(serializedPayload),
                Instant.parse(timestamp)
        );
    }
}
