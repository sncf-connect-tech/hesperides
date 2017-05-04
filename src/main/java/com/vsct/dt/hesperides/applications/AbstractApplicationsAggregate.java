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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vsct.dt.hesperides.applications.cache.ApplicationStoragePrefixInterface;
import com.vsct.dt.hesperides.applications.event.*;
import com.vsct.dt.hesperides.applications.properties.PropertiesRegistryInterface;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by emeric_martineau on 27/05/2016.
 */
public abstract class AbstractApplicationsAggregate extends SingleThreadAggregate implements Applications, PlatformEventBuilderInterface, ApplicationStoragePrefixInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationsAggregate.class);

    /**
     * Name of Aggregate
     */
    protected static final String NAME = "Applications";

    /**
     * Constructor to be used.
     *
     * @param eventBus
     * @param eventStore
     */
    public AbstractApplicationsAggregate(final EventBus eventBus, final EventStore eventStore) {
        super(eventBus, eventStore);
    }

    /**
     * Constructor to be used.
     *
     * @param eventBus
     * @param eventStore
     * @param userProvider
     */
    public AbstractApplicationsAggregate(final EventBus eventBus, final EventStore eventStore,
                                         final UserProvider userProvider) {
        super(eventBus, eventStore, userProvider);
    }

    /**
     * Plateform registry.
     *
     * @return
     */
    protected abstract PlatformRegistryInterface getPlatformRegistry();

    /**
     * Properties registry.
     *
     * @return
     */
    protected abstract PropertiesRegistryInterface getPropertiesRegistry();

    /**
     * Snapshot registry.
     *
     * @return
     */
    protected abstract SnapshotRegistryInterface getSnapshotRegistry();

    /**
     * Get an application with its name.
     * The application is not actually stored so we create it by assembling all the platforms corresponding to that application
     *
     * @param applicationName
     * @return the matching application or empty
     */
    @Override
    public Optional<ApplicationData> getApplication(final String applicationName) {
        final List<PlatformData> platforms = getPlatformRegistry().getPlatformsForApplication(applicationName);

        if (platforms.size() > 0) {
            return Optional.of(new ApplicationData(
                    applicationName,
                    platforms
            ));
        } else {
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
        return getPlatformRegistry().getPlatform(platformKey);
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

        final Optional<PlatformData> plt = getPlatformRegistry().getPlatform(
                new PlatformTimelineKey(platformKey, timestamp));

        if (plt.isPresent()) {
            return Optional.of(TimeStampedPlatformData
                    .withPlatform(plt.get())
                    .withTimestamp(timestamp).build());
        } else {
            return Optional.empty();
        }
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
        final PlatformCreatedCommand hc = new PlatformCreatedCommand(getPlatformRegistry(), platform);

        final PlatformCreatedEvent createdPlatformEventTry = this.tryAtomic(platform.getKey().getEntityName(), hc);

        return createdPlatformEventTry.getPlatform();
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
        Map<String, PropertiesData> existingPropertiesByPath = getPropertiesRegistry().getProperties(fromPlatformKey.getApplicationName(), fromPlatformKey.getName());

        return createPlatformFromExistingPlatformHandler(newPlatformToBeCreated, existingPlatform, existingPropertiesByPath);
    }

    /**
     *  Handler
     * @param platform
     * @return
     */
    private PlatformData createPlatformFromExistingPlatformHandler(final PlatformData platform,
                                                                   final PlatformData originPlatform,
                                                                   final Map<String, PropertiesData> originProperties){
        final PlatformCreatedFromExistingCommand hc = new PlatformCreatedFromExistingCommand(getPlatformRegistry(),
                getPropertiesRegistry(), platform, originPlatform, originProperties);

        this.tryAtomic(platform.getKey().getEntityName(), hc);

        //We return the platform from get method, this way we are sure to get the platform from the registry, ie the way it has been modified.
        return getPlatform(platform.getKey()).orElseThrow(() -> new MissingResourceException("Cannot get the created platform. This is not expected and should be reported"));
    }

    @Override
    public PlatformData updatePlatform(final PlatformData platform, final boolean isCopyingPropertiesForUpdatedModules) {
        final PlatformUpdatedCommand hc =
                new PlatformUpdatedCommand(getPlatformRegistry(), getPropertiesRegistry(), platform,
                        isCopyingPropertiesForUpdatedModules);

        final PlatformUpdatedEvent updatedPlatformEventTry = this.tryAtomic(platform.getKey().getEntityName(), hc);

        return updatedPlatformEventTry.getPlatform();
    }

    /**
     * Get properties for a platform with the specific path (example GSTWDI#WAS).
     *
     * @param platformKey
     * @param path
     * @return the properties or an empty property wrapper if none found
     */
    public PropertiesData getProperties(final PlatformKey platformKey, final String path) {
        return getPropertiesRegistry().getProperties(platformKey.getApplicationName(), platformKey.getName(), path).orElse(PropertiesData.empty());
    }

    @Override
    public PropertiesData getProperties(final PlatformKey platformKey, final String path, final long timestamp) {
        return getPropertiesRegistry().getProperties(platformKey.getApplicationName(), platformKey.getName(), path,
                timestamp).orElse(PropertiesData.empty());    }

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
    public PropertiesData createOrUpdatePropertiesInPlatform(final PlatformKey platformKey, final String path,
                                                             final PropertiesData properties,
                                                             final long platformVersionId, final String comment) {
        final PropertiesSavedCommand hc = new PropertiesSavedCommand(getPlatformRegistry(), getPropertiesRegistry(),
                platformKey, path, properties, platformVersionId, comment);

        final PropertiesSavedEvent propertiesSavedEvent = this.tryAtomic(platformKey.getEntityName(), hc);

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

        final String applicationName = platformKey.getApplicationName();
        final String platformName = platformKey.getName();

        this.getPlatform(platformKey).orElseThrow(() -> new MissingResourceException("Application/Platform " + applicationName + "/" + platformName + " does not exist"));

        final PropertiesData properties = this.getProperties(platformKey, propertiesPath);
        final PropertiesData globalProperties = this.getProperties(platformKey, "#");

        return properties.generateInstanceModel(globalProperties.getKeyValueProperties());
    }

    @Override
    public void delete(final PlatformKey key) {
        final PlatformDeletedCommand hc = new PlatformDeletedCommand(getPlatformRegistry(), getPropertiesRegistry(), key);

        this.tryAtomic(key.getEntityName(), hc);
    }

    /**
     * Take a snapshot of the platform, the timestamp will be System.currentMillisecond
     *
     * @param key
     * @return the timestamp of the snapshot
     */
    @Override
    public long takeSnapshot(PlatformKey key) {
        final long timestamp = System.currentTimeMillis();
        return takeSnapshot(key, timestamp);
    }

    /**
     * Used to replay the event specifying a timestamp
     *
     * @return
     */
    private long takeSnapshot(PlatformKey key, long timestamp) {
        final PlatformSnapshotCommand hc = new PlatformSnapshotCommand(getPlatformRegistry(), getPropertiesRegistry(),
                getSnapshotRegistry(), key, timestamp);

        final PlatformSnapshotEvent snapshotEvent = this.tryAtomic(key.getEntityName(), hc);

        return snapshotEvent.getTimestamp();
    }

    @Override
    public PlatformData restoreSnapshot(PlatformKey key, long timestamp) {
        final PlatformSnapshotKey snapshotKey = new PlatformSnapshotKey(timestamp, key);
        final Optional<PlatformSnapshot> optionalSnapshot
                = getSnapshotRegistry().getSnapshot(snapshotKey, PlatformSnapshot.class);

        if (optionalSnapshot.isPresent()) {
            return restoreSnapshot(timestamp, optionalSnapshot.get());
        } else {
            throw new MissingResourceException("Could not find snapshot " + timestamp + " for " + key);
        }
    }

    private PlatformData restoreSnapshot(long timestamp, PlatformSnapshot snapshot) {
        final PlatformKey key = snapshot.getPlatform().getKey();

        final PlatformSnapshotRestoreCommand hc = new PlatformSnapshotRestoreCommand(getPlatformRegistry(),
                getPropertiesRegistry(), timestamp, snapshot);

        this.tryAtomic(key.getEntityName(), hc);

        return getPlatformRegistry().getPlatform(key).get();
    }

    @Override
    public List<Long> getSnapshots(PlatformKey key) {
        // TODO Warning, Redis dependency !!! It's not good
        final String pattern = "snapshot-platform-" + key.getApplicationName() + "-" + key.getName() + "-*";
        final Set<String> snapshotKeysAsString = getSnapshotRegistry().getKeys(pattern);

        return snapshotKeysAsString.stream()
                .map(keyAsString -> Long.parseLong(keyAsString.replaceFirst(pattern, "")))
                .sorted((longA, longB) -> longA.compareTo(longB))
                .collect(Collectors.toList());
    }

    /**
     * Find the application matching the selector
     *
     * @param selector
     * @return the corresponding list of application
     */
    @Override
    public Collection<PlatformData> getApplicationsFromSelector(ApplicationSelector selector) {
        return getPlatformRegistry().getAllPlatforms().stream().filter(
                app -> selector.match(app)).collect(Collectors.toList());
    }

    /*
     * REPLAY LISTENERS
     */

    @Subscribe
    @Override
    public void replayPlatformCreatedEvent(final PlatformCreatedEvent event) {
        try {
            final PlatformData platform = event.getPlatform();
            this.createPlatform(platform);
        } catch (Exception e) {
            LOGGER.error("Error while replaying platform created event {}", e.getMessage());
        }
    }

    @Subscribe
    @Override
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
    @Override
    public void replayPlatformUpdatedEvent(final PlatformUpdatedEvent event) {
        try {
            final PlatformData platform = event.getPlatform();

            final PlatformKey key = platform.getKey();

            final PlatformData withDecrementedVersionID = PlatformData.withPlatformName(key.getName())
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
    @Override
    public void replayPropertiesSavedEvent(final PropertiesSavedEvent event) {
        try {
            final PlatformKey platformKey = PlatformKey.withName(event.getPlatformName())
                    .withApplicationName(event.getApplicationName())
                    .build();

            final PlatformData platform = this.getPlatform(platformKey).orElseThrow(() -> new RuntimeException("Cannot update properties in an unknown platform"));

            PlatformKey.withName(event.getPlatformName())
                    .withApplicationName(event.getApplicationName())
                    .build();

            this.createOrUpdatePropertiesInPlatform(platformKey, event.getPath(), event.getProperties(),
                    platform.getVersionID(), "PFR");
        } catch (Exception e) {
            LOGGER.error("Error while replaying properties saved event {}", e.getMessage());
        }
    }

    @Subscribe
    @Override
    public void replayPlateformeDeletedEvent(final PlatformDeletedEvent event) {
        try {

            final PlatformKey platformKey = PlatformKey.withName(event.getPlatformName())
                    .withApplicationName(event.getApplicationName())
                    .build();

            this.delete(platformKey);

        } catch (Exception e) {
            LOGGER.error("Error while replaying platform deleted event {}", e.getMessage());
        }
    }

    @Subscribe
    @Override
    public void replaySnapshotTakenEvent(final PlatformSnapshotEvent event) {
        try {

            final PlatformKey platformKey = PlatformKey.withName(event.getPlatformName())
                    .withApplicationName(event.getApplicationName())
                    .build();

            this.takeSnapshot(platformKey, event.getTimestamp());

        } catch (Exception e) {
            LOGGER.error("Error while replaying platform deleted event {}", e.getMessage());
        }
    }

    @Subscribe
    @Override
    public void replaySnapshotRestoredEvent(final PlatformSnapshotRestoreEvent event) {
        try {

            this.restoreSnapshot(event.getTimestamp(), event.getSnapshot());

        } catch (Exception e) {
            LOGGER.error("Error while replaying snapshot restore event {}", e.getMessage());
        }
    }

    /**
     * Get a set containing all platforms
     * @return an {@link java.util.Set} of {@link PlatformData}s
     */
    public Collection<PlatformData> getAll() {
        return getPlatformRegistry().getAllPlatforms();
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
     * Gets the number of platforms
     *
     * @return {@link Integer}
     */
    @Override
    public Collection<PlatformData> getAllPlatforms() {
        return getPlatformRegistry().getAllPlatforms();
    }
}
