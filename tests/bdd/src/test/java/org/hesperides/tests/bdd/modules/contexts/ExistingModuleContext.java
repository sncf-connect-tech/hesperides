package org.hesperides.tests.bdd.modules.contexts;

import com.google.common.collect.ImmutableSet;
import cucumber.api.java8.En;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.inputs.ModuleInput;
import org.hesperides.tests.bdd.CucumberSpringBean;

import java.net.URI;

public class ExistingModuleContext extends CucumberSpringBean implements En {

    private ModuleInput moduleInput;
    private URI moduleLocation;
    private Module.Key moduleKey;

    public ExistingModuleContext() {

        Given("^an existing module$", () -> {
            createModule("test", "1.0.0", Module.Type.workingcopy);
        });

        Given("^an existing released module$", () -> {
            createModule("test", "1.0.0", Module.Type.release);
        });

        Given("^an existing module with multiple versions$", () -> {
            for (int i = 0; i < 6; i++) {
                createModule("test", "1.0." + i, Module.Type.workingcopy);
            }
        });

        Given("^an existing module working copy and its release$", () -> {
            createModule("test", "1.0.0", Module.Type.workingcopy);
            createModule("test", "1.0.0", Module.Type.release);
        });

        Given("^a list of existing modules$", () -> {
            for (int i = 0; i < 20; i++) {
                rest.getTestRest().postForLocation("/modules", new ModuleInput("test-" + i, "1.0." + i, true, ImmutableSet.of(), 0L));
            }
        });

        Given("^a list of existing modules released$", () -> {
            for (int i = 0; i < 20; i++) {
                rest.getTestRest().postForLocation("/modules", new ModuleInput("test-" + i, "1.0." + i, false, ImmutableSet.of(), 0L));
            }
        });
    }

    private void createModule(String name, String version, TemplateContainer.Type type) {
        moduleKey = new Module.Key(name, version, type);
        moduleInput = new ModuleInput(moduleKey.getName(), moduleKey.getVersion(), moduleKey.isWorkingCopy(), ImmutableSet.of(), -1L);
        moduleLocation = rest.postForLocationReturnAbsoluteURI("/modules", moduleInput);
    }

    public Module.Key getModuleKey() {
        return moduleKey;
    }

    public URI getModuleLocation() {
        return moduleLocation;
    }
}
