package org.hesperides.application;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.domain.modules.commands.CopyModuleCommand;
import org.hesperides.domain.modules.commands.CreateModuleCommand;
import org.hesperides.domain.modules.commands.Module;
import org.hesperides.domain.modules.queries.ModuleByIdQuery;
import org.hesperides.domain.modules.queries.ModuleView;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
public class Modules {

    private final CommandGateway commandGateway;

    private final QueryGateway queryGateway;

    public Modules(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
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

    public Optional<ModuleView> getModule(Module.Key moduleKey) {
        try {
            return Optional.ofNullable(queryGateway.send(new ModuleByIdQuery(moduleKey), ModuleView.class).get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
