package org.hesperides.infrastructure.redis.eventstores;

import com.thoughtworks.xstream.XStream;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "redis", name = "codec", havingValue = "default", matchIfMissing = true)
class DefaultCodec implements Codec {

    private static final XStream xStream = new XStream();

    @Override
    public String code(DomainEventMessage<?> event) {
        return xStream.toXML(event);
    }

    @Override
    public List<DomainEventMessage<?>> decode(String aggregateIdentifier, long firstSequenceNumber, List<String> data) {
        xStream.setClassLoader(Thread.currentThread().getContextClassLoader());
        List<DomainEventMessage<?>> list = new ArrayList<>();

        for (String s: data) {
            DomainEventMessage<?> event = (DomainEventMessage<?>) xStream.fromXML(s);
            list.add(event);
        }

        return list;
    }
}
