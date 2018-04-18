package org.hesperides.tests.bdd.modules.contexts;

import com.google.common.collect.ImmutableSet;
import cucumber.api.java8.En;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.presentation.inputs.ModuleInput;
import org.hesperides.tests.bdd.CucumberSpringBean;

import java.net.URI;

public class ExistingModuleContext extends CucumberSpringBean implements En {

    private ModuleInput moduleInput;
    private URI moduleLocation;
    private Module.Key moduleKey;

    public ExistingModuleContext() {
        Given("^an existing module$", () -> {
            moduleKey = new Module.Key("test", "123", Module.Type.workingcopy);
            moduleInput = new ModuleInput(moduleKey.getName(), moduleKey.getVersion(), moduleKey.isWorkingCopy(), ImmutableSet.of(), 1L);
            moduleLocation = rest.postForLocationReturnAbsoluteURI("/modules", moduleInput);
        });
    }

    public Module.Key getModuleKey() {
        return moduleKey;
    }

    public URI getModuleLocation() {
        return moduleLocation;
    }
}
