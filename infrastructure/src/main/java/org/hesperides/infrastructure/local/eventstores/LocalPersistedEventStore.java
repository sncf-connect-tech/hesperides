package org.hesperides.infrastructure.local.eventstores;

import com.thoughtworks.xstream.XStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * In memory event storage if local profile is used
 */
@Slf4j
@Component
@Profile("local_persisted_to_file") // <-- n'utilise pas celui ci pour les tests
@Primary
// <-- si on a en même temps le profil local utilisé, on prend ce bean plutôt que LocalEventStore
public class LocalPersistedEventStore extends EmbeddedEventStore {

    private final static XStream xStream = new XStream();
    private final static File localEventStoreFile = new File(FileUtils.getTempDirectory(), "hesperide_local_event_store.xml");

    public LocalPersistedEventStore() throws IOException {
        super(loadFromDisk());
    }

    private static EventStorageEngine loadFromDisk() throws IOException {
        // charge les events depuis le disque, pour voir.
        if (localEventStoreFile.exists()) {
            log.debug("Load event store from file {}", localEventStoreFile);
            xStream.setClassLoader(Thread.currentThread().getContextClassLoader());
            return (InMemoryEventStorageEngine) xStream.fromXML(FileUtils.readFileToString(localEventStoreFile, Charset.defaultCharset()));
        }
        // si pas de fichier, on créer en mémoire.
        return new InMemoryEventStorageEngine();
    }

    @PreDestroy
    public void saveToDisk() throws IOException {
        log.debug("Save event store to file {}", localEventStoreFile);
        String xml = xStream.toXML(this.storageEngine());
        FileUtils.writeStringToFile(localEventStoreFile, xml, Charset.defaultCharset());
    }
}
