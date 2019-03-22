package org.hesperides.core.application.files;

import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.platforms.queries.views.InstanceView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.junit.Test;

import java.util.List;

import static org.hesperides.core.application.files.FileUseCases.valorizeWithModuleAndGlobalAndInstanceProperties;
import static org.hesperides.core.application.files.RandomPlatformViewGenerator.*;

public class FileUseCasesTest {

    @Test(timeout = 1000)
    public void testValorizeWithModuleAndGlobalAndInstanceProperties() {
        // Cas observé : plateforme = ~ 15 globals + 1-4 instance properties + 32 deployedModules avec valued properties : 28 in range 1-45 + 4 in range 200-400
        // répartition des valorisations de propriétés de module simple utilisant des propriétés d'instances : 2x37, 2x6 + le reste entre 0 & 2
        PlatformView platform = genPlatformView(7,
                new DeployedModuleProfile(2, 200, 18),
                new DeployedModuleProfile(2, 200, 3),
                new DeployedModuleProfile(28, 22, 1)
        );
        List<AbstractPropertyView> modulePropertiesModels = genModulePropertiesModels(350);

        DeployedModuleView firstDeployedModule = platform.getDeployedModules().get(0);
        InstanceView firstInstance = firstDeployedModule.getInstances().get(0);
        valorizeWithModuleAndGlobalAndInstanceProperties(
                "/tmp/{{hesperides.instance.name}}/{{hesperides.application.version}}.conf",
                platform,
                firstDeployedModule.getModulePath(),
                firstDeployedModule.getModuleKey(),
                modulePropertiesModels,
                firstInstance.getName(),
                false
        );
    }

}
