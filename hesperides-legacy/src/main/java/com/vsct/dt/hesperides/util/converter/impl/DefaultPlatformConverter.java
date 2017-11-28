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

package com.vsct.dt.hesperides.util.converter.impl;

import com.vsct.dt.hesperides.resources.ApplicationModule;
import com.vsct.dt.hesperides.resources.Instance;
import com.vsct.dt.hesperides.resources.Platform;
import com.vsct.dt.hesperides.templating.platform.AbstractPlatformData;
import com.vsct.dt.hesperides.templating.platform.ApplicationModuleData;
import com.vsct.dt.hesperides.templating.platform.InstanceData;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.util.converter.InstanceConverter;
import com.vsct.dt.hesperides.util.converter.PlatformConverter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by emeric_martineau on 27/10/2015.
 */
public final class DefaultPlatformConverter implements PlatformConverter {
    private final InstanceConverter instanceConverter;

    public DefaultPlatformConverter() {
        this.instanceConverter = new DefaultInstanceConverter();
    }

    public DefaultPlatformConverter(final InstanceConverter instanceConverter) {
        this.instanceConverter = instanceConverter;
    }

    /**
     * Convert PlatformDat object to Platform object
     *
     * @param platform input
     * @return output
     */
    @Override
    public PlatformData toPlatformData(final Platform platform) {
        Set<ApplicationModuleData> listAppModule = new HashSet<>(platform.getModules().size());

        for (ApplicationModule appModule : platform.getModules()) {
            listAppModule.add(
                    ApplicationModuleData
                            .withApplicationName(appModule.getName())
                            .withVersion(appModule.getVersion())
                            .withPath(appModule.getPath())
                            .withId(appModule.getId())
                            .withInstances(toListInstanceData(appModule.getInstances()))
                            .setWorkingcopy(appModule.isWorkingCopy())
                            .build());
        }

        return PlatformData.withPlatformName(platform.getPlatformName())
                .withApplicationName(platform.getApplicationName())
                .withApplicationVersion(platform.getApplicationVersion())
                .withModules(listAppModule)
                .withVersion(platform.getVersionID())
                .setProduction(platform.isProduction())
                .build();
    }

    /**
     * Convert PlatformData object to Platform
     *
     * @param platformData input
     * @return output
     */
    @Override
    public Platform toPlatform(final AbstractPlatformData platformData) {
        Set<ApplicationModule> listAppModule = new HashSet<>(platformData.getModules().size());

        for (ApplicationModuleData appModule : platformData.getModules()) {
            listAppModule.add(
                    new ApplicationModule(appModule.getName(), appModule.getVersion(),
                            appModule.isWorkingCopy(), appModule.getPath(),
                            toListInstance(appModule.getInstances()), appModule.getId()));
        }

        return new Platform(platformData.getPlatformName(),
                platformData.getApplicationName(),
                platformData.getApplicationVersion(),
                platformData.isProduction(),
                listAppModule, platformData.getVersionID());
    }

    /**
     * Convert a list of Instance to a list of InstanceData
     *
     * @param listInstance list
     * @return list
     */
    private Set<InstanceData> toListInstanceData(final Set<Instance> listInstance) {
        Set<InstanceData> newSetInstance = new HashSet<>(listInstance.size());

        for (Instance instance : listInstance) {
            newSetInstance.add(
                    instanceConverter.toInstanceData(instance)
            );
        }

        return newSetInstance;
    }

    /**
     * Convert a list of Instance to a list of InstanceData
     *
     * @param listInstance list
     * @return list
     */
    private Set<Instance> toListInstance(final Set<InstanceData> listInstance) {
        Set<Instance> newSetInstance = new HashSet<>(listInstance.size());

        for (InstanceData instance : listInstance) {
            newSetInstance.add(
                    instanceConverter.toInstance(instance)
            );
        }

        return newSetInstance;
    }


    @Override
    public InstanceConverter getInstanceConverter() {
        return instanceConverter;
    }
}
