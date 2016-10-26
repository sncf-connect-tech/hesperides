package com.vsct.dt.hesperides.templating.modules;

import java.util.Collection;
import java.util.Optional;

/**
 * Module registry interface.
 *
 * Created by emeric_martineau on 15/01/2016.
 */
public interface ModuleRegistryInterface {
    void createOrUpdateModule(Module module);

    boolean existsModule(ModuleKey key);

    Optional<Module> getModule(ModuleKey key);

    void deleteModule(ModuleKey key);

    Collection<Module> getAllModules();

    /**
     * Remove item from cache.
     *
     * @param key
     */
    void removeFromCache(ModuleKey key);

    /**
     * Remove all data in cache.
     */
    void removeAllCache();
}
