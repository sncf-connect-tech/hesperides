package com.vsct.dt.hesperides.applications.event;

import com.vsct.dt.hesperides.applications.PlatformRegistryInterface;
import com.vsct.dt.hesperides.applications.PlatformUpdatedEvent;
import com.vsct.dt.hesperides.applications.properties.PropertiesRegistryInterface;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.templating.platform.ApplicationModuleData;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by emeric_martineau on 09/05/2016.
 */
public class PlatformUpdatedCommand extends AbstractPlatformEvent<PlatformUpdatedEvent> {
    private final PlatformData platform;
    private final PlatformRegistryInterface platformRegistry;
    private final boolean isCopyingPropertiesForUpdatedModules;
    private final PropertiesRegistryInterface propertiesRegistry;

    /**
     * The new platform to store in cache.
     */
    private PlatformData updatedPlatform;

    /**
     * If need update properties.
     */
    private boolean updateProperties = false;

    /**
     * Module to update.
     */
    private ApplicationModuleData module;

    /**
     * Properties to update.
     */
    private PropertiesData properties;

    public PlatformUpdatedCommand(final PlatformRegistryInterface platformRegistry,
                                  final PropertiesRegistryInterface propertiesRegistry,
                                  final PlatformData platform,
                                  final boolean isCopyingPropertiesForUpdatedModules) {
        this.platform = platform;
        this.platformRegistry = platformRegistry;
        this.isCopyingPropertiesForUpdatedModules = isCopyingPropertiesForUpdatedModules;
        this.propertiesRegistry = propertiesRegistry;
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
     * @return the updated platform value object with an incremented versionID
     */
    @Override
    public PlatformUpdatedEvent apply() {
        String applicationName = platform.getApplicationName();
        String platformName = platform.getPlatformName();
        String applicationVersion = platform.getApplicationVersion();
        long platformVID = platform.getVersionID();
        boolean isProductionPlatform = platform.isProduction();

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
                    if (detectIfEntityProvidedHasUpdatedThisModuleAndCopyPropertiesIfNeeded(
                            isCopyingPropertiesForUpdatedModules, applicationName, platformName, module, existingModule))
                        break; //We wont find another matching module, just exit loop
                }
            }

            /* Create the new platform entity */
            updatedPlatform = PlatformData.withPlatformName(platformName)
                    .withApplicationName(applicationName)
                    .withApplicationVersion(applicationVersion)
                    .withModules(modulesWithIds)
                    .withVersion(platformVID + 1)
                    .setProduction(isProductionPlatform)
                    .build();

            return new PlatformUpdatedEvent(applicationName, updatedPlatform, isCopyingPropertiesForUpdatedModules);
        } else {
            throw new MissingResourceException("PlatformData " + platform + " does not exist");
        }
    }

    @Override
    public void complete() {
        this.platformRegistry.createOrUpdatePlatform(updatedPlatform);

        if (updateProperties) {
            propertiesRegistry.createOrUpdateProperties(updatedPlatform.getApplicationName(),
                    updatedPlatform.getPlatformName(), module.getPropertiesPath(), properties);
        }
    }

    private boolean detectIfEntityProvidedHasUpdatedThisModuleAndCopyPropertiesIfNeeded(
            final boolean isCopyingPropertiesForUpdatedModules,
            final String applicationName, String platformName,
            final ApplicationModuleData module,
            final ApplicationModuleData existingModule) {
        if (existingModule.getId() == module.getId()) {
            /* Comparing properties path is equivalent to test wether path, module name or version is different between modules */
            if (!existingModule.getPropertiesPath().equals(module.getPropertiesPath()) && isCopyingPropertiesForUpdatedModules) {
                final Optional<PropertiesData> propertiesOptional = propertiesRegistry.getProperties(applicationName, platformName, existingModule.getPropertiesPath());

                if (propertiesOptional.isPresent()) {
                    this.updateProperties = true;
                    this.properties = propertiesOptional.get();
                    this.module = module;
                }
            }

            return true;
        }

        return false;
    }
}
