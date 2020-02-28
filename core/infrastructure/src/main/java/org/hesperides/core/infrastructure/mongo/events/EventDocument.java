package org.hesperides.core.infrastructure.mongo.events;

import com.thoughtworks.xstream.XStream;
import lombok.Data;
import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.security.UserEvent;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import static org.hesperides.core.infrastructure.mongo.Collections.DOMAINEVENTS;

@Data
@Document(collection = DOMAINEVENTS)
public class EventDocument {

    @Indexed
    private String aggregateIdentifier;
    private String type;
    private int sequenceNumber;
    private String serializedPayload;
    private String timestamp;
    private String payloadType;
    private String serializedMetadata;
    private String eventIdentifier;

    public EventView toEventView() {
        XStream xStream = new XStream();
        // Afin d'éviter le message "Security framework of XStream
        // not initialized, XStream is probably vulnerable"
        // cf. https://stackoverflow.com/questions/44698296/security-framework-of-xstream-not-initialized-xstream-is-probably-vulnerable
        xStream.allowTypesByWildcard(new String[]{
                "org.hesperides.core.domain.**"
        });

        return new EventView(
                payloadType,
                (UserEvent) xStream.fromXML(serializedPayload),
                Instant.parse(timestamp)
        );
    }
}
