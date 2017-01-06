/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides.applications;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vsct.dt.hesperides.exception.runtime.DuplicateResourceException;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.resources.Platform;
import com.vsct.dt.hesperides.security.UserContext;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.SingleThreadAggregate;
import com.vsct.dt.hesperides.storage.UserProvider;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.platform.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 10/12/2014.
 */
public class ApplicationsAggregate extends SingleThreadAggregate implements Applications {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationsAggregate.class);
    /**
     * The platform registry holds all platforms instances, it hides how platforms are stored.
     */
    private final PlatformRegistry   platformRegistry;
    /**
     * The properties registry holds all properties sets, it hides how properties are stored.
     */
    private final PropertiesRegistry propertiesRegistry;

    /**
     * Reference to the eventstore
     * eventstore is supposed to be managed by super class, but we need to inject it in child class ApplicationsAggregateFromTimestamp
     */
    protected final EventStore eventstore;

    /**
     * Used to make snapshots, it hides implementation details about how snapshots are stored
     * Since there might be a lot of snapshot, we dont want to keep them inmemory like platformRegistry and propertiesRegistry
     */
    private final SnapshotRegistry snapshotRegistry;

    /**
     * Little cache to retain platform loaded at a specific point in time
     * It is very likely for users to perform read operations on that platform once they loaded it
     */
    LoadingCache<CacheKey, PlateformTimelineDelegate> plateformeTimelineCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<CacheKey, PlateformTimelineDelegate>() {
                @Override
                public PlateformTimelineDelegate load(CacheKey key) throws Exception {
                    return new PlateformTimelineDelegate(key.platformKey, key.timestamp);
                }
            });

    private static class CacheKey {

        private final PlatformKey platformKey;
        private final long        timestamp;

        public CacheKey(PlatformKey platformKey, long timestamp) {
            this.platformKey = platformKey;
            this.timestamp = timestamp;
        }

        @Override
        public int hashCode() {
            return Objects.hash(platformKey, timestamp);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            CacheKey other = (CacheKey) obj;
            return Objects.equals(this.platformKey, other.platformKey)
                    && Objects.equals(this.timestamp, other.timestamp);
        }
    }

    /**
     * Constructor to be used.
     *
     * @param eventBus
     * @param eventStore
     * @param snapshotRegistry
     */
    public ApplicationsAggregate(final EventBus eventBus, final EventStore eventStore, final SnapshotRegistry snapshotRegistry) {
        super("Applications", eventBus, eventStore);
        this.platformRegistry = new PlatformRegistry();
        this.propertiesRegistry = new PropertiesRegistry();
        this.eventstore = eventStore;
        this.snapshotRegistry = snapshotRegistry;
    }

    public ApplicationsAggregate(final EventBus eventBus, final EventStore eventStore, final SnapshotRegistry snapshotRegistry, final UserProvider userProvider) {
        super("Applications", eventBus, eventStore, userProvider);
        this.platformRegistry = new PlatformRegistry();
        this.propertiesRegistry = new PropertiesRegistry();
        this.eventstore = eventStore;
        this.snapshotRegistry = snapshotRegistry;
    }

    /**
     * Method helpers thatcan be used to do some processing based on all platforms
     * We provide Value Objects to the consumer, and not entities, entities are manipulated only within Applications class
     *
     * @param consumer
     */
    public void withAllPlatforms(Consumer<PlatformData> consumer) {
        platformRegistry.getAllPlatforms().stream()
                .forEach(valueObject -> consumer.accept(valueObject));
    }


    /**
     * Get a set containing all platforms
     * @return an {@link java.util.Set} of {@link PlatformData}s
     */
    public Collection<PlatformData> getAll() {
        return platformRegistry.getAllPlatforms();
    }

    /**
     * Get an application with its name.
     * The application is not actually stored so we create it by assembling all the platforms corresponding to that application
     *
     * @param applicationName
     * @return the matching application or empty
     */
    @Override
    public Optional<ApplicationData> getApplication(final String applicationName) {
        List<PlatformData> platforms = this.platformRegistry.getPlatformsForApplication(applicationName);
        if (platforms.size() > 0) {
            return Optional.of(new ApplicationData(
                    applicationName,
                    platforms
            ));
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Get a platform with its name and its application name.
     *
     * @param platformKey
     * @return The corresponding platform or empty
     */
    @Override
    public Optional<PlatformData> getPlatform(final PlatformKey platformKey) {
        return this.platformRegistry.getPlatform(platformKey);
    }

    /**
     * Get a platform at a specific moment in time defined by timestamp param
     *
     * @param platformKey
     * @param timestamp
     * @return
     */
    @Override
    public Optional<TimeStampedPlatformData> getPlatform(final PlatformKey platformKey, final long timestamp) {
        CacheKey cacheKey = new CacheKey(platformKey, timestamp);
        PlateformTimelineDelegate delegate;
        try {
            delegate = plateformeTimelineCache.get(cacheKey);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to retrieve PlateformeTimelineDelegate for key " + cacheKey + " from cache. This should have not happened and needs further investigations.");
        }
        return delegate.getPlatform().map(platform -> TimeStampedPlatformData
                .withPlatform(platform)
                .withTimestamp(timestamp).build());
    }

    /**
     * Creates a platform with given modules.
     * It will provide an id to all modules that have no id or id set to 0
     *
     * @param platform
     * @return the created platform value object (with a versionID at 1)
     */
    @Override
    public PlatformData createPlatform(final PlatformData platform) {

        String applicationName = platform.getApplicationName();
        String platformName = platform.getPlatformName();

        String applicationVersion = platform.getApplicationVersion();
        boolean isProductionPlatform = platform.isProduction();

        PlatformCreatedEvent createdPlatformEventTry = this.tryAtomic(platform.getKey().getEntityName(), () -> {

            Optional<PlatformData> existingPlatform = this.platformRegistry.getPlatform(platform.getKey());
            if (existingPlatform.isPresent()) {
                throw new DuplicateResourceException("PlatformData " + platform + " already exists");
            }

            Set<ApplicationModuleData> modulesWithIds = generateSetOfModulesWithIds(platform.getModules());

            PlatformData newPlatform = PlatformData.withPlatformName(platformName)
                    .withApplicationName(applicationName)
                    .withApplicationVersion(applicationVersion)
                    .withModules(modulesWithIds)
                    .withVersion(1L)
                    .setProduction(isProductionPlatform)
                    .build();

            this.platformRegistry.createOrUpdate(newPlatform);

            return new PlatformCreatedEvent(applicationName, newPlatform);
        });

        return createdPlatformEventTry.getPlatform();
    }

    private Set<ApplicationModuleData> generateSetOfModulesWithIds(Set<ApplicationModuleData> modules, Set<Integer> existingIds) {
        //Try to give an id to modules if they are missing
        existingIds.addAll(modules.stream().map(module -> module.getId()).collect(Collectors.toSet()));

        return modules.stream().map(module -> {
            //0 is the default value given to a module
            //We consider id 0 should never be given to a module
            if (module.getId() == 0) {
                int id = nextIdExcluding(existingIds);
                existingIds.add(id);

                return ApplicationModuleData
                        .withApplicationName(module.getName())
                        .withVersion(module.getVersion())
                        .withPath(module.getPath())
                        .withId(id)
                        .withInstances(module.getInstances())
                        .setWorkingcopy(module.isWorkingCopy())
                        .build();
            }
            else {
                return module;
            }
        }).collect(Collectors.toSet());
    }

    private Set<ApplicationModuleData> generateSetOfModulesWithIds(Set<ApplicationModuleData> modules) {
        return generateSetOfModulesWithIds(modules, Sets.newHashSet());
    }

    private int nextIdExcluding(Set<Integer> forbiddenIds) {
        int i = 1;
        while (forbiddenIds.contains(i)) {
            i++;
        }
        return i;
    }

    /**
     * Method to create a platform from another
     * We also need to copy the properties
     * This implementation is not atomic and creates as many events as if we did the operation manually by single steps
     * It should be sufficient for the use case, but it can fail if the new platform is concurrently modified, which is pretty much impossible
     *
     * @param platform
     * @param fromPlatformKey
     * @return
     */
    @Override
    public PlatformData createPlatformFromExistingPlatform(final PlatformData platform, final PlatformKey fromPlatformKey) {

        // Getting existing platform
        PlatformData existingPlatform = getPlatform(fromPlatformKey).orElseThrow(() -> new MissingResourceException("There is no existing platform " + fromPlatformKey + " to build from"));

        // new platform's key
        PlatformKey key = platform.getKey();

        // Building the platform to be created, this contains all the stuff
        PlatformData newPlatformToBeCreated = PlatformData.withPlatformName(key.getName())
                .withApplicationName(key.getApplicationName())
                .withApplicationVersion(platform.getApplicationVersion())
                .withModules(existingPlatform.getModules())
                .withVersion(1L)
                .setProduction(platform.isProduction())
                .build();

        // Get the properties by path
        Map<String, PropertiesData> existingPropertiesByPath = propertiesRegistry.getProperties(fromPlatformKey.getApplicationName(), fromPlatformKey.getName());

        return createPlatformFromExistingPlatformHandler ( newPlatformToBeCreated, existingPlatform, existingPropertiesByPath );
    }

    /**
     *  Handler
     * @param platform
     * @return
     */
    public PlatformData createPlatformFromExistingPlatformHandler ( final PlatformData platform, final PlatformData originPlatform, Map<String, PropertiesData> originProperties){
        String applicationName = platform.getApplicationName();
        String platformName = platform.getPlatformName();

        String applicationVersion = platform.getApplicationVersion();
        boolean isProductionPlatform = platform.isProduction();

        PlatformCreatedFromExistingEvent createdPlatformExistingEventTry = this.tryAtomic(platform.getKey().getEntityName(), () -> {

            Set<ApplicationModuleData> modulesWithIds = generateSetOfModulesWithIds(platform.getModules());

            PlatformData newPlatform = PlatformData.withPlatformName(platformName)
                    .withApplicationName(applicationName)
                    .withApplicationVersion(applicationVersion)
                    .withModules(modulesWithIds)
                    .withVersion(1L)
                    .setProduction(isProductionPlatform)
                    .build();

            this.platformRegistry.createOrUpdate(newPlatform);

            for (Map.Entry<String, PropertiesData> entry : originProperties.entrySet()){
                this.propertiesRegistry.createOrUpdate(applicationName, platformName, entry.getKey(), entry.getValue());
            }

            return new PlatformCreatedFromExistingEvent(applicationName, newPlatform, originPlatform, originProperties);
        });

        //return createdPlatformExistingEventTry.getPlatform();
        //We return the platform from get method, this way we are sure to get the platform from the registry, ie the way it has been modified.
        return getPlatform(platform.getKey()).orElseThrow(() -> new MissingResourceException("Cannot get the created platform. This is not expected and should be reported"));
    }

    /**
     * Updates a platform with the platform provided. The applicationName and the platformName are not changed
     * even if the given platform argument provides different ones.
     * It will try to detect module for which path has changed, to make properties follow
     * If param isCopyingPropertiesForUpdatedModules is set to true, it will detect module that have same id but for which name/version has changed
     * <p>
     * An important point to consider is that id attribution does not have to be identical through different code version (ie it does not matter if we give ids differently between different versions of hesperides)
     * The point is that the recorded event WILL HAVE IDS so when replaying, everything acts as if the user always provided ids
     * The only thing that matter is to be sure not to give an id that was in the platform just before the update
     * for exemple, you remove one module and add two modules without ids, none of these new modules should have the id of the deleted module
     * because it would be confused when trying to detect updated modules. And even if we put the module detection before giving id, this would still be a problem when replaying events !!)
     * <p>
     * This is so important, lets show some examples :
     * <p>
     * Let say we created a platform with modules m1, m2 and m3, respectively with ids 1, 2, 3
     * When replaying PlatformCreatedEvent, it will use ids 1, 2, 3 so we will have our platform
     * <p>
     * This shows what could be wrong :
     * <p>
     * USER WORKFLOW
     * <p>
     * PLATFORM UPDATE (Delete m3, Add m4 and m5, Update m2 and make properties follow)
     * | | | | |
     * | | | | |
     * V V V V V
     * INPUT
     * copyProperties ? true     Event saved with thoose ids
     * m1(1) --->   m1(1)                 --------------------> m1(1)
     * m2(2) --->   m2(2)                 - copy properties !-> m2(2)
     * m3(3) --->                         -----delete---------> m3(x)
     * m4(0)                 --------------------> m4(3)
     * m5(0)                 --------------------> m5(4)
     * <p>
     * In the workflow above, note that id 3 has been given to m4 AFTER module update detection
     * So, we only copied m2 which is what we wanted
     * <p>
     * Now, when replaying, this is what happens
     * INPUT
     * copyProperties ? true
     * m1(1) ---> m1(1)                  ---------------------> m1(1)
     * m2(2) ---> m2(2)                  - copy properties !--> m2(2)
     * m3(3) --->                        ---------------------> m3(x)
     * m4(3)                  - copy properties !--> m4(3) OUPS !!!!!!!
     * m5(4)                  ---------------------> m5(4)
     * <p>
     * This would have not happened if m4 was given id 4 and m5 id 5...
     *
     * @param platform
     * @param isCopyingPropertiesForUpdatedModules
     * @return the updated platform value object with an incremented versionID
     */
    @Override
    public PlatformData updatePlatform(final PlatformData platform, final boolean isCopyingPropertiesForUpdatedModules) {

        String applicationName = platform.getApplicationName();
        String platformName = platform.getPlatformName();
        String applicationVersion = platform.getApplicationVersion();
        long platformVID = platform.getVersionID();
        boolean isProductionPlatform = platform.isProduction();

        PlatformUpdatedEvent updatedPlatformEventTry = this.tryAtomic(platform.getKey().getEntityName(), () -> {

            Optional<PlatformData> optionalExistingPlatform = this.platformRegistry.getPlatform(platform.getKey());

            if (optionalExistingPlatform.isPresent()) {

                PlatformData existingPlatform = optionalExistingPlatform.get();

                existingPlatform.tryCompareVersionID(platformVID);

                    /* Gather all provided ids and existing ids
                    * The goal is to provide ids to new modules if needed. These ids should not be one already provided or existing
                    * because we test module update with ids
                    */
                Set<Integer> existingIds = existingPlatform.getModules().stream().map(module -> module.getId()).collect(Collectors.toSet());
                Set<ApplicationModuleData> modulesWithIds = generateSetOfModulesWithIds(platform.getModules(), existingIds);

                    /* Detect updated modules, based on the id
                     * Two things might have been updated
                     * - The Path -> always change the properties associated
                     * - The name and version of the module -> change properties only if explicitely asked
                     */
                for (ApplicationModuleData module : platform.getModules()) {
                    //Is there a module with same name and path in entity ?
                    for (ApplicationModuleData existingModule : existingPlatform.getModules()) {
                        if (detectIfEntityProvidedHasUpdatedThisModuleAndCopyPropertiesIfNeeded(isCopyingPropertiesForUpdatedModules, applicationName, platformName, module, existingModule))
                            break; //We wont find another matching module, just exit loop
                    }
                }

                /* Create the new platform entity */
                PlatformData updatedPlatform = PlatformData.withPlatformName(platformName)
                        .withApplicationName(applicationName)
                        .withApplicationVersion(applicationVersion)
                        .withModules(modulesWithIds)
                        .withVersion(platformVID + 1)
                        .setProduction(isProductionPlatform)
                        .build();

                this.platformRegistry.createOrUpdate(updatedPlatform);

                return new PlatformUpdatedEvent(applicationName, updatedPlatform, isCopyingPropertiesForUpdatedModules);

            }
            else {
                throw new MissingResourceException("PlatformData " + platform + " does not exist");
            }

        });

        return updatedPlatformEventTry.getPlatform();
    }

    private boolean detectIfEntityProvidedHasUpdatedThisModuleAndCopyPropertiesIfNeeded(boolean isCopyingPropertiesForUpdatedModules, String applicationName, String platformName, ApplicationModuleData module, ApplicationModuleData existingModule) {
        if (existingModule.getId() == module.getId()) {
                /* Comparing properties path is equivalent to test wether path, module name or version is different between modules */
            if (!existingModule.getPropertiesPath().equals(module.getPropertiesPath()) && isCopyingPropertiesForUpdatedModules) {
                Optional<PropertiesData> propertiesOptional = propertiesRegistry.getProperties(applicationName, platformName, existingModule.getPropertiesPath());
                if (propertiesOptional.isPresent()) {
                    propertiesRegistry.createOrUpdate(applicationName, platformName, module.getPropertiesPath(), propertiesOptional.get());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Get properties for a platform with the specific path (example ADMFOO#WAS).
     *
     * @param platformKey
     * @param path
     * @return the properties or an empty property wrapper if none found
     */
    public PropertiesData getProperties(final PlatformKey platformKey, final String path) {
        return this.propertiesRegistry.getProperties(platformKey.getApplicationName(), platformKey.getName(), path).orElse(PropertiesData.empty());
    }

    @Override
    public PropertiesData getProperties(final PlatformKey platformKey, final String path, final long timestamp) {
        CacheKey cacheKey = new CacheKey(platformKey, timestamp);
        PlateformTimelineDelegate delegate;
        try {
            delegate = plateformeTimelineCache.get(cacheKey);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to retrieve PlateformeTimelineDelegate for key " + cacheKey + " from cache. This should have not happened and needs further investigations.");
        }
        return delegate.getProperties(path);
    }

    @Override
    public PropertiesData getSecuredProperties(PlatformKey platformKey, String path, HesperidesPropertiesModel model) {
        // No security need for that
        return this.getProperties(platformKey, path);
    }

    @Override
    public PropertiesData getSecuredProperties(PlatformKey platformKey, String path, long timestamp, HesperidesPropertiesModel model) {
        // Security need for that
        return this.getProperties(platformKey, path, timestamp);
    }

    /**
     * Create or Update properties for a platform at the given path.
     *
     * @param platformKey
     * @param path
     * @param properties
     * @param platformVersionId
     * @return the properties value object
     */
    @Override
    public PropertiesData createOrUpdatePropertiesInPlatform(final PlatformKey platformKey, final String path, final PropertiesData properties, final long platformVersionId, final String comment) {

        String applicationName = platformKey.getApplicationName();
        String platformName = platformKey.getName();
        long platformVID = platformVersionId;

        PropertiesSavedEvent propertiesSavedEvent = this.tryAtomic(platformKey.getEntityName(), () -> {

            Optional<PlatformData> optionalPlatform = platformRegistry.getPlatform(platformKey);
            if (optionalPlatform.isPresent()) {

                PlatformData platform = optionalPlatform.get();
                platform.tryCompareVersionID(platformVID);
                //Properties are immutable, it is ok to store them directly
                propertiesRegistry.createOrUpdate(applicationName, platformName, path, properties);

                PlatformData updatedPlatform = PlatformData.withPlatformName(platform.getPlatformName())
                        .withApplicationName(platform.getApplicationName())
                        .withApplicationVersion(platform.getApplicationVersion())
                        .withModules(platform.getModules())
                        .withVersion(platform.getVersionID() + 1)
                        .setProduction(platform.isProduction())
                        .build();

                platformRegistry.createOrUpdate(updatedPlatform);

                return new PropertiesSavedEvent(applicationName, platformName, path, properties, comment);

            }
            else {
                throw new MissingResourceException("Cannot create properties because platform " + applicationName + "/" + platformName + " does not exist");
            }

        });

        return propertiesSavedEvent.getProperties();
    }

    /**
     * Find the model of properties to be evaluated for a given instance.
     *
     * @param platformKey
     * @param propertiesPath
     * @return the corresponding model
     */
    @Override
    public InstanceModel getInstanceModel(final PlatformKey platformKey, final String propertiesPath) {

        String applicationName = platformKey.getApplicationName();
        String platformName = platformKey.getName();

        this.getPlatform(platformKey).orElseThrow(() -> new MissingResourceException("Application/Platform " + applicationName + "/" + platformName + " does not exist"));

        PropertiesData properties = this.getProperties(platformKey, propertiesPath);
        PropertiesData globalProperties = this.getProperties(platformKey, "#");

        return properties.generateInstanceModel(globalProperties.getKeyValueProperties());
    }

    /**
     * Find the application matching the selector
     *
     * @param selector
     * @return the corresponding list of application
     */
    @Override
    public Collection<PlatformData> getApplicationsFromSelector(ApplicationSelector selector) {
        return platformRegistry.getAllPlatforms().stream().filter(app -> selector.match(app)).collect(Collectors.toList());
    }

    /**
     * Gets the number of platforms
     *
     * @return {@link Integer}
     */
    @Override
    public Integer getAllPlatformsCount() {
        return platformRegistry.getAllPlatforms().stream().collect(Collectors.toList()).size();
    }

    /**
     * Gets the number of applications
     *
     * @return {@link Integer}
     */
    @Override
    public Integer getAllApplicationsCount() {
        return platformRegistry.getAllPlatforms().stream().filter(distinctByKey(app -> app.getApplicationName())).collect(Collectors.toList()).size();
    }

    /**
     * Gets the number of modules
     *
     * @return {@link Integer}
     */
    @Override
    public Integer getAllModulesCount() {
        return platformRegistry.getAllPlatforms().stream().filter(distinctByKey(app -> app.getModules())).collect(Collectors.toList()).size();
    }

    /**
     * Function used for extract distinct values from collection.
     *
     * @param keyExtractor
     * @param <T>
     * @return the filtered collection
     */
    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @Override
    public void delete(PlatformKey key) {
        this.tryAtomic(key.getEntityName(), () -> {

            String platformName = key.getName();
            String applicationName = key.getApplicationName();

            Optional<PlatformData> platformOptional = this.getPlatform(key);
            if (platformOptional.isPresent()) {

                //remove all properties
                Iterator<String> it = propertiesRegistry.getProperties(applicationName, platformName).keySet().iterator();
                while (it.hasNext()) {
                    it.next();
                    it.remove();
                }

                //remove platform
                platformRegistry.delete(key);

                return new PlatformDeletedEvent(applicationName, platformName);

            }
            else {
                throw new MissingResourceException(key + " does not exists");
            }

        });
    }

    /**
     * Take a snapshot of the platform, the timestamp will be System.currentMillisecond
     *
     * @param key
     * @return the timestamp of the snapshot
     */
    @Override
    public long takeSnapshot(PlatformKey key) {
        long timestamp = System.currentTimeMillis();
        return takeSnapshot(key, timestamp);
    }

    /**
     * Used to replay the event specifying a timestamp
     *
     * @return
     */
    private long takeSnapshot(PlatformKey key, long timestamp) {
        PlatformSnapshotEvent snapshotEvent = this.tryAtomic(key.getEntityName(), () -> {

            Optional<PlatformData> platformOptional = this.getPlatform(key);
            if (platformOptional.isPresent()) {

                //Create the platform object
                //We only save the properties related to modules defined in the platform. Properties from older modules (still in properties registry) are not kept
                PlatformData platform = platformOptional.get();
                Map<String, PropertiesData> allPlatformProperties = new HashMap<>();
                platform.getModules().forEach(module -> {
                    String path = module.getPropertiesPath();
                    Optional<PropertiesData> optionalProperties = this.propertiesRegistry.getProperties(key.getApplicationName(), key.getName(), path);
                    if (optionalProperties.isPresent()) allPlatformProperties.put(path, optionalProperties.get());
                });

                PlatformSnapshot platformSnapshot = new PlatformSnapshot(platform, allPlatformProperties);

                //Put the snapshot in the snapshot registry
                PlatformSnapshotKey snapshotKey = new PlatformSnapshotKey(timestamp, key);
                snapshotRegistry.createSnapshot(snapshotKey, platformSnapshot);

                //Fire an event
                return new PlatformSnapshotEvent(timestamp, key.getApplicationName(), key.getName());

            }
            else {
                throw new MissingResourceException(key + " does not exists");
            }


        });

        return snapshotEvent.getTimestamp();
    }

    @Override
    public PlatformData restoreSnapshot(PlatformKey key, long timestamp) {
        PlatformSnapshotKey snapshotKey = new PlatformSnapshotKey(timestamp, key);
        Optional<PlatformSnapshot> optionalSnapshot = snapshotRegistry.getSnapshot(snapshotKey, PlatformSnapshot.class);
        if (optionalSnapshot.isPresent()) {

            return restoreSnapshot(timestamp, optionalSnapshot.get());

        }
        else throw new MissingResourceException("Could not find snapshot " + timestamp + " for " + key);
    }

    @Override
    public UserContext getUserContext() {
        return null;
    }

    private PlatformData restoreSnapshot(long timestamp, PlatformSnapshot snapshot) {
        PlatformKey key = snapshot.getPlatform().getKey();

        this.tryAtomic(key.getEntityName(), () -> {

            PlatformData snapshotedPlatform = snapshot.getPlatform();
            //We need to be cautious with version id to avoid messing up everything
            //First get the platformVid
            Optional<PlatformData> optionalExistingPlatform = platformRegistry.getPlatform(key);
            long vid = 0;

            if (optionalExistingPlatform.isPresent()) {
                //PlatformData exist we get the vid
                PlatformData existingPlatform = optionalExistingPlatform.get();
                vid = existingPlatform.getVersionID();
            }

            PlatformData update = PlatformData.withPlatformName(snapshotedPlatform.getPlatformName())
                    .withApplicationName(snapshotedPlatform.getApplicationName())
                    .withApplicationVersion(snapshotedPlatform.getApplicationVersion())
                    .withModules(snapshotedPlatform.getModules())
                    .withVersion(vid + 1)
                    .setProduction(snapshotedPlatform.isProduction())
                    .build();

            platformRegistry.createOrUpdate(update);

            //Add all the properties
            snapshot.getProperties().forEach((path, properties) ->
                propertiesRegistry.createOrUpdate(key.getApplicationName(), key.getName(), path, properties)
            );

            return new PlatformSnapshotRestoreEvent(timestamp, snapshot);

        });


        return platformRegistry.getPlatform(key).get();

    }

    @Override
    public List<Long> getSnapshots(PlatformKey key) {
        String pattern = "snapshot-platform-" + key.getApplicationName() + "-" + key.getName() + "-*";
        Set<String> snapshotKeysAsString = snapshotRegistry.getKeys(pattern);
        return snapshotKeysAsString.stream()
                .map(keyAsString -> Long.parseLong(keyAsString.replaceFirst(pattern, "")))
                .sorted((longA, longB) -> longA.compareTo(longB))
                .collect(Collectors.toList());
    }

    @Override
    protected String getStreamPrefix() {
        return "platform";
    }

    /*
     * REPLAY LISTENERS
     */

    @Subscribe
    public void replayPlatformCreatedEvent(final PlatformCreatedEvent event) {
        try {
            PlatformData platform = event.getPlatform();
            this.createPlatform(platform);
        } catch (Exception e) {
            LOGGER.error("Error while replaying platform created event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayPlatformCreatedFromExistingEvent(final PlatformCreatedFromExistingEvent event) {
        try {
            PlatformData platform = event.getPlatform();
            PlatformData originPlatform = event.getOriginPlatform();
            Map<String, PropertiesData> orginProperties = event.getOriginProperties();
            this.createPlatformFromExistingPlatformHandler(platform, originPlatform, orginProperties);
        } catch (Exception e) {
            LOGGER.error("Error while replaying platform created from existing event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayPlatformUpdatedEvent(final PlatformUpdatedEvent event) {
        try {
            PlatformData platform = event.getPlatform();

            PlatformKey key = platform.getKey();

            PlatformData withDecrementedVersionID = PlatformData.withPlatformName(key.getName())
                    .withApplicationName(key.getApplicationName())
                    .withApplicationVersion(platform.getApplicationVersion())
                    .withModules(platform.getModules())
                    .withVersion(platform.getVersionID() - 1)
                    .setProduction(platform.isProduction())
                    .build();

            this.updatePlatform(withDecrementedVersionID, event.isCopyingPropertiesForUpgradedModules());
        } catch (Exception e) {
            LOGGER.error("Error while replaying platform updated event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayPropertiesSavedEvent(final PropertiesSavedEvent event) {
        try {
            PlatformKey platformKey = PlatformKey.withName(event.getPlatformName())
                    .withApplicationName(event.getApplicationName())
                    .build();

            PlatformData platform = this.getPlatform(platformKey).orElseThrow(() -> new RuntimeException("Cannot update properties in an unknown platform"));

            PlatformKey platformKeyWithVersionID = PlatformKey.withName(event.getPlatformName())
                    .withApplicationName(event.getApplicationName())
                    .build();

            this.createOrUpdatePropertiesInPlatform(platformKey, event.getPath(), event.getProperties(), platform.getVersionID(), "PFR");
        } catch (Exception e) {
            LOGGER.error("Error while replaying properties saved event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replayPlateformeDeletedEvent(final PlatformDeletedEvent event) {
        try {

            PlatformKey platformKey = PlatformKey.withName(event.getPlatformName())
                    .withApplicationName(event.getApplicationName())
                    .build();

            this.delete(platformKey);

        } catch (Exception e) {
            LOGGER.error("Error while replaying platform deleted event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replaySnapshotTakenEvent(final PlatformSnapshotEvent event) {
        try {

            PlatformKey platformKey = PlatformKey.withName(event.getPlatformName())
                    .withApplicationName(event.getApplicationName())
                    .build();

            this.takeSnapshot(platformKey, event.getTimestamp());

        } catch (Exception e) {
            LOGGER.error("Error while replaying platform deleted event {}", e.getMessage());
        }
    }

    @Subscribe
    public void replaySnapshotRestoredEvent(final PlatformSnapshotRestoreEvent event) {
        try {

            this.restoreSnapshot(event.getTimestamp(), event.getSnapshot());

        } catch (Exception e) {
            LOGGER.error("Error while replaying snapshot restore event {}", e.getMessage());
        }
    }

    /**
     * Easy way to replay an application state from the beginning of time to a specific timestamp
     * The structure is not really costly for the purpose of hesperides
     * Creating a subclass helps to provide an isolated structure and reuse the whole code !
     * We don't want to do anything else than reading a platform at a specific point in time
     * That implementation needs to stay private
     */

    private class PlateformTimelineDelegate {

        private final long        lookAtTimestamp;
        private final PlatformKey lookAtPlatform;

        /**
         * We use an inner aggregate designed for our purpose
         * we dont really want to propagate events, so the eventbus will be a freshly created instance
         * We also set the isReplaying flag to true to avoid storing any event
         */
        private final TimelineApplicationsAggregate innerAggregate;

        private final class TimelineApplicationsAggregate extends ApplicationsAggregate {
            public TimelineApplicationsAggregate(EventStore eventStore) {
                //We inject a null Snasphot registry because it is of no use in this context
                //PlateformetimelineDelegate will hide call to methods using SnapshotRegistry, thus preventing unexpected errors
                super(new EventBus(), eventStore, null);
                this.isReplaying.set(true);
            }

            private void replayEntity(String entityName, long lookAtTimestamp) {
                this.replayBus.register(this);
                this.eventstore.withEvents(getStreamPrefix() + "-" + entityName, lookAtTimestamp, event -> replayBus.post(event));
                this.replayBus.unregister(this);
            }
        }

        public PlateformTimelineDelegate(PlatformKey lookAtPlatform, long lookAtTimestamp) {
            innerAggregate = new TimelineApplicationsAggregate(ApplicationsAggregate.this.eventstore);

            this.lookAtTimestamp = lookAtTimestamp;
            this.lookAtPlatform = lookAtPlatform;

            //Load aggregate
            innerAggregate.replayEntity(lookAtPlatform.getEntityName(), lookAtTimestamp);
        }

        public Optional<PlatformData> getPlatform() {
            return innerAggregate.getPlatform(lookAtPlatform);
        }

        public PropertiesData getProperties(String path) {
            return innerAggregate.getProperties(lookAtPlatform, path);
        }

    }

}
