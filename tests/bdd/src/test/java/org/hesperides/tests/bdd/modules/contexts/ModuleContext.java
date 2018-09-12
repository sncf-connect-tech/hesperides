package org.hesperides.tests.bdd.modules.contexts;

import cucumber.api.java8.En;
import cucumber.api.java8.StepdefBody;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.commons.tools.HesperidesTestRestTemplate;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class ModuleContext implements En {

    private TemplateContainer.Key moduleKey;

    @Autowired
    private TechnoContext technoContext;

    @Autowired
    private HesperidesTestRestTemplate rest;

    public ModuleContext() {
        Given("^an existing module$", (StepdefBody.A0) this::createModule);

        Given("^a module that is released$", () -> {
            createModule();
            releaseModule();
        });
    }


    public TemplateContainer.Key getModuleKey() {
        return moduleKey;
    }

    private void createModule() {
        ModuleIO moduleInput = new ModuleBuilder().build();
        createModule(moduleInput);
    }

    public ResponseEntity<ModuleIO> createModule(ModuleIO moduleInput) {
        ResponseEntity<ModuleIO> response = rest.getTestRest().postForEntity("/modules", moduleInput, ModuleIO.class);
        ModuleIO moduleOutput = response.getBody();
        moduleKey = new Module.Key(moduleOutput.getName(), moduleOutput.getVersion(), TemplateContainer.getVersionType(moduleOutput.isWorkingCopy()));
        return response;
    }

    public String getNamespace() {
        return "modules#" + moduleKey.getName() + "#" + moduleKey.getVersion() + "#" + moduleKey.getVersionType().name().toUpperCase();
    }

    public String getModuleURI(TemplateContainer.Key moduleKey) {
        return String.format("/modules/%s/%s/%s", moduleKey.getName(), moduleKey.getVersion(), moduleKey.getVersionType());
    }

    public String getModuleURI() {
        return getModuleURI(moduleKey);
    }

    public ResponseEntity<ModuleIO> releaseModule() {
        return releaseModule(moduleKey.getName(), moduleKey.getVersion());
    }

    public ResponseEntity<ModuleIO> releaseModule(String moduleName, String moduleVersion) {
        return rest.getTestRest().postForEntity("/modules/create_release?module_name={moduleName}&module_version={moduleVersion}",
                null, ModuleIO.class, moduleName, moduleVersion);
    }

    public ResponseEntity<ModuleIO> updateModule(ModuleIO moduleInput) {
        return rest.putForEntity("/modules", moduleInput, ModuleIO.class);
    }
}
