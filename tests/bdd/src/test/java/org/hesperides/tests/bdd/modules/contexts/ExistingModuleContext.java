package org.hesperides.tests.bdd.modules.contexts;

import com.google.common.collect.ImmutableSet;
import cucumber.api.java.en.Given;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.presentation.controllers.ModuleInput;
import org.hesperides.tests.bdd.CucumberSpringBean;

import java.net.URI;

public class ExistingModuleContext extends CucumberSpringBean {

    private ModuleInput moduleInput;
    private URI moduleLocation;
    private Module.Key moduleKey;

    @Given("^an existing module$")
    public void anExistingModule() throws Throwable {
        moduleKey = new Module.Key("test", "123", Module.Type.workingcopy);
        moduleInput = new ModuleInput(moduleKey.getName(), moduleKey.getVersion(), moduleKey.isWorkingCopy(), ImmutableSet.of(), 1L);
        moduleLocation = template.postForLocationReturnAbsoluteURI("/modules", moduleInput);
    }

    public Module.Key getModuleKey() {
        return moduleKey;
    }

    public URI getModuleLocation() {
        return moduleLocation;
    }
}
