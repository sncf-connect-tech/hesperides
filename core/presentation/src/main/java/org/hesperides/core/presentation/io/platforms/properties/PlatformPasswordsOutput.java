/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.core.presentation.io.platforms.properties;

import lombok.Value;
import org.hesperides.core.domain.platforms.queries.views.properties.PlatformProperties;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Value
public class PlatformPasswordsOutput {

    String applicationName;
    String platformName;
    boolean isProductionPlatform;
    List<DeployedModule> deployedModules;

    public PlatformPasswordsOutput(PlatformProperties platform) {
        applicationName = platform.getApplicationName();
        platformName = platform.getPlatformName();
        isProductionPlatform = platform.isProductionPlatform();
        deployedModules = DeployedModule.fromDomainInstances(platform.getDeployedModules());
    }

    public static List<PlatformPasswordsOutput> fromDomainInstances(List<PlatformProperties> platformsPasswords) {
        return platformsPasswords.stream()
                .map(PlatformPasswordsOutput::new)
                .collect(toList());
    }

    @Value
    public static class DeployedModule {
        String propertiesPath;
        boolean isArchivedModule;
        List<Password> passwords;

        public DeployedModule(PlatformProperties.DeployedModule deployedModule) {
            propertiesPath = deployedModule.getPropertiesPath();
            isArchivedModule = deployedModule.isArchivedModule();
            passwords = Password.fromDomainInstances(deployedModule.getProperties());
        }

        public static List<DeployedModule> fromDomainInstances(List<PlatformProperties.DeployedModule> deployedModules) {
            return deployedModules.stream().map(DeployedModule::new).collect(toList());
        }
    }

    @Value
    public static class Password {
        String propertyName;
        String propertyValue;

        public Password(PlatformProperties.Property password) {
            propertyName = password.getName();
            propertyValue = password.getValue();
        }

        public static List<Password> fromDomainInstances(List<PlatformProperties.Property> passwords) {
            return passwords.stream()
                    .map(Password::new)
                    .collect(toList());
        }
    }
}
