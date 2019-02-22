package org.hesperides.core.infrastructure.inmemory.platforms;

import org.hesperides.core.infrastructure.MinimalPlatformRepository;
import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformDocument;

import java.util.Optional;

/*
 * Cette classe se substitue à un MongoRepository<PlatformDocument, String>
 * afin d'appliquer toutes les opérations de création / mise à jour / suppression
 * concernant une unique plateforme sur un seul objet PlatformDocument.
 *
 * Le but est de ré-appliquer un historique d'évênements pour reconstruire une plateforme
 * dans son état à un instant T.
 */
public class InmemoryPlatformRepository implements MinimalPlatformRepository {

    private PlatformDocument currentPlatformDocument = null;

    public PlatformDocument getCurrentPlatformDocument() {
        return currentPlatformDocument;
    }

    @Override
    public PlatformDocument save(PlatformDocument platformDoc) {
        currentPlatformDocument = platformDoc;
        return platformDoc;
    }

    @Override
    public Optional<PlatformDocument> findById(String id) {
        if (currentPlatformDocument == null || !id.equals(currentPlatformDocument.getId())) {
            throw new UnsupportedOperationException();
        }
        return Optional.of(currentPlatformDocument);
    }

    @Override
    public void deleteById(String id) {
        if (currentPlatformDocument == null || !id.equals(currentPlatformDocument.getId())) {
            throw new UnsupportedOperationException();
        }
        currentPlatformDocument = null;
    }
}