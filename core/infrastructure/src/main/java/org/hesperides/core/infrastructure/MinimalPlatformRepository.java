package org.hesperides.core.infrastructure;

import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformDocument;

import java.util.Optional;

/*
 * Cette interface minimale est un sous-ensemble de MongoRepository<PlatformDocument, String>.
 * Elle permet d'identifier clairement quelles méthodes sont nécessaires aux @EventHandler
 * dans MongoPlatformProjectionRepository.
 */
public interface MinimalPlatformRepository {

    PlatformDocument save(PlatformDocument platformDoc);

    Optional<PlatformDocument> findById(String id);

    void deleteById(String id);
}
