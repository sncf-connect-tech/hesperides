package org.hesperides.core.presentation.io.platforms.properties;

import lombok.Value;
import org.hesperides.core.domain.platforms.queries.views.properties.PlatformProperties;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Value
public class FlatPasswordsOutput {
    String passwordName;
    String passwordValue;
    String applicationName;
    String platformName;
    boolean isProductionPlatform;
    String propertiesPath;
    boolean isArchivedModule;

    public static List<FlatPasswordsOutput> fromDomainInstances(List<PlatformProperties> applicationsPasswords) {
        return applicationsPasswords.stream()
                .flatMap(platform -> platform.getDeployedModules().stream()
                        .flatMap(deployedModule -> deployedModule.getProperties().stream()
                                .map(password -> new FlatPasswordsOutput(
                                        password.getName(),
                                        password.getValue(),
                                        platform.getApplicationName(),
                                        platform.getPlatformName(),
                                        platform.isProductionPlatform(),
                                        deployedModule.getPropertiesPath(),
                                        deployedModule.isArchivedModule()
                                ))))
                .collect(toList());
    }
}
