package org.hesperides.application;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hesperides.domain.modules.CopyModuleCommand;
import org.hesperides.domain.modules.CreateModuleCommand;
import org.hesperides.domain.modules.Module;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
public class Modules {

    private final CommandGateway commandGateway;

    public Modules(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public Module.Key createWorkingCopy(String moduleName, String moduleVersion) {

        try {
            return (Module.Key) commandGateway.send(new CreateModuleCommand(new Module.Key(moduleName, moduleVersion)))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Module.Key createWorkingCopyFrom(String name, String version, String from_module_name, String from_module_version) {
        try {
            return (Module.Key) commandGateway.send(new CopyModuleCommand(new Module.Key(name, version),
                    new Module.Key(from_module_name, from_module_version))).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Module> getModule(Module.Key moduleKey) {
        return Optional.empty();
    }
}
