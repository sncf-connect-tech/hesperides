package com.vsct.dt.hesperides.templating.modules.template.event;

import com.google.common.cache.CacheLoader;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.StoragePrefixInterface;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import org.slf4j.Logger;

import java.util.List;

/**
 * Created by emeric_martineau on 15/01/2016.
 */
public abstract class AbstractTemplateCacheLoader<K, T extends TemplateContainerInterface>
        extends CacheLoader<K, T> implements StoragePrefixInterface {
    /**
     * Store of event.
     */
    private final EventStore store;

    /**
     * Nb event before store in cache.
     */
    private final long nbEventBeforePersiste;

    /**
     * Return logger.
     *
     * @return
     */
    protected abstract Logger getLogger();

    /**
     * Constructor.
     *
     * @param store event store
     */
    public AbstractTemplateCacheLoader(final EventStore store, final long nbEventBeforePersiste) {
        this.store = store;
        this.nbEventBeforePersiste = nbEventBeforePersiste;
    }

    /**
     * Return store.
     *
     * @return event store
     */
    protected EventStore getStore() {
        return store;
    }

    /**
     * Return Nb event before store in cache.
     *
     * @return long
     */
    protected long getNbEventBeforePersiste() {
        return this.nbEventBeforePersiste;
    }

    /**
     * Create event builder that listen store event.
     *
     * @return event builder. Must be a new instance at each call.
     */
    protected abstract T createEventBuilder();

    /**
     * Return object load name for log.
     *
     * @return name
     */
    protected abstract String getObjectLoadName();

    /**
     * Generate key to search in database.
     *
     * @param moduleKey module key
     *
     * @return db key
     */
    protected String generateDbKey(final ModuleKey moduleKey) {
        // Redis key pattern to search all application platform
        return String.format("%s-%s",
                getStreamPrefix(), moduleKey.getEntityName());
    }

    /**
     * Check if tempalte name exists.
     *
     * @param namespace name space
     *
     * @return true/false
     */
    public boolean isNamespaceExist(final String namespace) {
        final String redisKey = generateDbKey(new ModuleKey(namespace));

        getLogger().debug("Search if key '{}' associate to namespace '{}' for {} exists in store.", redisKey,
                getObjectLoadName(), namespace);

        return !getStore().getStreamsLike(redisKey).isEmpty();
    }


    /**
     * Return list of modules.
     *
     * @return list of modules
     */
    public abstract List<Template> getAllTemplates();

    /**
     * Store object in snapshot.
     *
     * @param namespace key of cache (same as cache.get(K))
     * @param object object
     */
    public void saveSnapshot(final String namespace, final T object) {
        final ModuleKey moduleKey = new ModuleKey(namespace);

        // Don't store snapshot of release, cause release contain only one event
        if (moduleKey.isWorkingCopy()) {
            final String redisKey = generateDbKey(moduleKey);

            // Now store snapshot
            store.storeSnapshot(redisKey, object, nbEventBeforePersiste);
        }
    }

    /**
     * Store object in snapshot.
     *
     * @param namespace key of cache (same as cache.get(K))
     * @param object object
     */
    public void forceSaveSnapshot(final String namespace, final T object, final long nbEvent) {
        final ModuleKey moduleKey = new ModuleKey(namespace);

        // Don't store snapshot of release, cause release contain only one event
        if (moduleKey.isWorkingCopy()) {
            final String redisKey = generateDbKey(moduleKey);

            // Now store snapshot
            store.createSnapshot(redisKey, object, nbEvent);
        }
    }
}
