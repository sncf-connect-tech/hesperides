package org.hesperides.tests.bdd.modules.contexts;

import com.google.common.collect.ImmutableList;
import cucumber.api.java8.En;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class ExistingModuleContext extends CucumberSpringBean implements En {

    private Module.Key moduleKey;

    public ExistingModuleContext() {

        Given("^an existing module working copy$", () -> {
            createWorkingCopy("test", "1.0.0");
        });

        Given("^an existing released module$", () -> {
            createWorkingCopyAndRelease("test", "1.0.0");
        });

        Given("^this module is being released$", () -> {
            createRelease("test", "1.0.0");
        });

        Given("^an existing module working copy and its release$", () -> {
            createWorkingCopyAndRelease("test", "1.0.0");
        });

        Given("^an existing module working copy with multiple versions$", () -> {
            for (int i = 0; i < 6; i++) {
                createWorkingCopy("test", "1.0." + i);
            }
        });

        Given("^a list of existing modules working copy$", () -> {
            for (int i = 0; i < 20; i++) {
                createWorkingCopy("test-" + i, "1.0." + i);
            }
        });

        Given("^a list of existing modules released$", () -> {
            for (int i = 0; i < 20; i++) {
                createWorkingCopyAndRelease("test-" + i, "1.0." + i);
            }
        });
    }

    private void createWorkingCopyAndRelease(String name, String version) {
        createWorkingCopy(name, version);
        createRelease(name, version);
    }

    private void createWorkingCopy(String name, String version) {
        ModuleIO moduleInput = new ModuleIO(name, version, true, ImmutableList.of(), -1L);
        ResponseEntity<ModuleIO> response = rest.getTestRest().postForEntity("/modules", moduleInput, ModuleIO.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        setModuleKeyFromModuleOutput(response.getBody());
    }

    private void createRelease(String moduleName, String moduleVersion) {
        ResponseEntity<ModuleIO> response = rest.getTestRest().postForEntity("/modules/create_release?module_name={moduleName}&module_version={moduleVersion}&release_version={releaseVersion}",
                null, ModuleIO.class, moduleName, moduleVersion, moduleVersion);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        setModuleKeyFromModuleOutput(response.getBody());
    }

    private void setModuleKeyFromModuleOutput(ModuleIO moduleOutput) {
        moduleKey = new TemplateContainer.Key(moduleOutput.getName(), moduleOutput.getVersion(), moduleOutput.isWorkingCopy() ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release);
    }

    public Module.Key getModuleKey() {
        return moduleKey;
    }

    public String getModuleLocation() {
        return String.format("/modules/%s/%s/%s", moduleKey.getName(), moduleKey.getVersion(), moduleKey.getVersionType());
    }
}
