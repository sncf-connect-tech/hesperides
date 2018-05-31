package org.hesperides.tests.bdd.modules.contexts;

import com.google.common.collect.ImmutableList;
import cucumber.api.java8.En;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.io.ModuleIO;
import org.hesperides.presentation.io.TechnoIO;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.ModuleSamples;
import org.hesperides.tests.bdd.technos.contexts.ExistingTechnoContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ModuleContext extends CucumberSpringBean implements En {

    private Module.Key moduleKey;

    @Autowired
    private ExistingTechnoContext existingTechnoContext;

    public ModuleContext() {

        Given("^an existing module$", () -> {
            createModule();
        });

        Given("^a module that is released$", () -> {
            createModule();
            releaseModule();
        });

//        Given("^an existing module containing a template", () -> {
//            createWorkingCopy("test", "1.0.0");
//        });
//
//        Given("^an existing module containing this techno$", () -> {
//            createWorkingCopy("test", "1.0.0", existingTechnoContext.getTechnoKey());
//        });
//
//        Given("^an existing released module$", () -> {
//            createWorkingCopyAndRelease("test", "1.0.0");
//        });
//
//        Given("^this module is being released$", () -> {
//            createRelease("test", "1.0.0");
//        });
//
//        Given("^an existing module and its release$", () -> {
//            createWorkingCopyAndRelease("test", "1.0.0");
//        });
//
//        Given("^an existing module with multiple versions$", () -> {
//            for (int i = 0; i < 6; i++) {
//                createWorkingCopy("test", "1.0." + i);
//            }
//        });
//
//        Given("^a list of existing modules working copy$", () -> {
//            for (int i = 0; i < 20; i++) {
//                createWorkingCopy("test-" + i, "1.0." + i);
//            }
//        });
//
//        Given("^a list of existing modules released$", () -> {
//            for (int i = 0; i < 20; i++) {
//                createWorkingCopyAndRelease("test-" + i, "1.0." + i);
//            }
//        });
    }

//    private void createWorkingCopy(String name, String version) {
//        createWorkingCopy(name, version, null);
//    }
//
//    private void createWorkingCopy(String name, String version, TemplateContainer.Key technoKey) {
//        List<TechnoIO> technos = technoKey != null ? Arrays.asList(new TechnoIO(technoKey.getName(), technoKey.getVersion(), technoKey.isWorkingCopy())) : null;
//        ModuleIO moduleInput = new ModuleIO(name, version, true, technos, -1L);
//        ResponseEntity<ModuleIO> response = rest.getTestRest().postForEntity("/modules", moduleInput, ModuleIO.class);
//        assertEquals(HttpStatus.CREATED, response.getStatusCode());
//        setModuleKeyFromModuleOutput(response.getBody());
//    }
//
//    private void createWorkingCopyAndRelease(String name, String version) {
//        createWorkingCopy(name, version);
//        createRelease(name, version);
//    }
//
//    private void createRelease(String moduleName, String moduleVersion) {
//        ResponseEntity<ModuleIO> response = rest.getTestRest().postForEntity("/modules/create_release?module_name={moduleName}&module_version={moduleVersion}&release_version={releaseVersion}",
//                null, ModuleIO.class, moduleName, moduleVersion, moduleVersion);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        setModuleKeyFromModuleOutput(response.getBody());
//    }
//
//    private void setModuleKeyFromModuleOutput(ModuleIO moduleOutput) {
//    }

    private void createModule() {
        ModuleIO moduleInput = ModuleSamples.getModuleInputWithDefaultValues();
        createModule(moduleInput);
    }

    public ResponseEntity<ModuleIO> createModule(ModuleIO moduleInput) {
        ResponseEntity<ModuleIO> response = rest.getTestRest().postForEntity("/modules", moduleInput, ModuleIO.class);
        ModuleIO moduleOutput = response.getBody();
        moduleKey = new TemplateContainer.Key(moduleOutput.getName(), moduleOutput.getVersion(), TemplateContainer.getVersionType(moduleOutput.isWorkingCopy()));
        return response;
    }

    public String getNamespace() {
        return "modules#" + moduleKey.getName() + "#" + moduleKey.getVersion() + "#" + moduleKey.getVersionType().name().toUpperCase();
    }

    public String getModuleURI() {
        return String.format("/modules/%s/%s/%s", moduleKey.getName(), moduleKey.getVersion(), moduleKey.getVersionType());
    }

    public ResponseEntity<ModuleIO> retrieveExistingTemplate() {
        return rest.getTestRest().getForEntity(getModuleURI(), ModuleIO.class);
    }

    public ResponseEntity<String[]> getModulesNames() {
        return rest.getTestRest().getForEntity("/modules", String[].class);
    }

    public ResponseEntity<String[]> getModuleTypes() {
        return rest.getTestRest().getForEntity("/modules/{moduleName}/{moduleVersion}", String[].class, moduleKey.getName(), moduleKey.getVersion());
    }

    public ResponseEntity<String[]> getModuleVersions() {
        return rest.getTestRest().getForEntity("/modules/{moduleName}", String[].class, moduleKey.getName());
    }

    public ResponseEntity<ModuleIO> releaseModule() {
        return releaseModule(moduleKey.getName(), moduleKey.getVersion());
    }

    public ResponseEntity<ModuleIO> releaseModule(String moduleName, String moduleVersion) {
        return rest.getTestRest().postForEntity("/modules/create_release?module_name={moduleName}&module_version={moduleVersion}",
                null, ModuleIO.class, moduleName, moduleVersion);
    }

    public ResponseEntity<ModuleIO> copyModule(ModuleIO moduleInput) {
        return rest.getTestRest().postForEntity("/modules?from_module_name={moduleName}&from_module_version={moduleVersion}&from_is_working_copy={isWorkingCopy}",
                moduleInput, ModuleIO.class,
                moduleKey.getName(), moduleKey.getVersion(), moduleKey.isWorkingCopy());
    }

    public ResponseEntity<ModuleIO> updateModule(ModuleIO moduleInput) {
        return rest.putForEntity("/modules", moduleInput, ModuleIO.class);
    }
}
