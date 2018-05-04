package org.hesperides.tests.bdd.modules.contexts;

import com.google.common.collect.ImmutableSet;
import cucumber.api.java8.En;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.inputs.ModuleInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

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
        ModuleInput moduleInput = new ModuleInput(name, version, true, ImmutableSet.of(), -1L);
        URI uri = rest.postForLocationReturnAbsoluteURI("/modules", moduleInput);
        ResponseEntity<ModuleView> response = rest.getTestRest().getForEntity(uri, ModuleView.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        setModuleKeyFromModuleView(response.getBody());
    }

    private void createRelease(String moduleName, String moduleVersion) {
        ResponseEntity<ModuleView> response = rest.getTestRest().postForEntity("/modules/create_release?module_name={moduleName}&module_version={moduleVersion}&release_version={releaseVersion}",
                null, ModuleView.class, moduleName, moduleVersion, moduleVersion);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        setModuleKeyFromModuleView(response.getBody());
    }

    private void setModuleKeyFromModuleView(ModuleView module) {
        moduleKey = new TemplateContainer.Key(module.getName(), module.getVersion(), module.isWorkingCopy() ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release);
    }

    public Module.Key getModuleKey() {
        return moduleKey;
    }

    public String getModuleLocation() {
        return String.format("/modules/%s/%s/%s", moduleKey.getName(), moduleKey.getVersion(), moduleKey.getVersionType());
    }
}
