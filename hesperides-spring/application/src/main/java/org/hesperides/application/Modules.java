package org.hesperides.application;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hesperides.domain.Module;
import org.hesperides.domain.modules.CopyModuleCommand;
import org.hesperides.domain.modules.CreateModuleCommand;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class Modules {

    private final CommandGateway commandGateway;

    public Modules(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public Module createWorkingCopy(String moduleName, String moduleVersion) {

        try {
            return (Module) commandGateway.send(new CreateModuleCommand(new org.hesperides.domain.modules.Module.Key(moduleName, moduleVersion)))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Module createWorkingCopyFrom(String name, String version, String from_module_name, String from_module_version) {
        try {
            return (Module) commandGateway.send(new CopyModuleCommand(new org.hesperides.domain.modules.Module.Key(name, version),
                    new org.hesperides.domain.modules.Module.Key(from_module_name, from_module_version))).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
