package org.hesperides.application;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.domain.modules.Module;
import org.hesperides.domain.modules.ModuleType;
import org.hesperides.domain.modules.commands.CopyModuleCommand;
import org.hesperides.domain.modules.commands.CreateModuleCommand;
import org.hesperides.domain.modules.queries.ModuleByIdQuery;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.modules.queries.ModulesNamesQuery;
import org.springframework.stereotype.Component;

import java.util.List;
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

    public Module.Key createWorkingCopy(Module.Key newModuleKey) {

        try {
            return (Module.Key) commandGateway.send(new CreateModuleCommand(newModuleKey)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Module.Key createWorkingCopyFrom(Module.Key from, Module.Key to) {
        try {
            return (Module.Key) commandGateway.send(new CopyModuleCommand(to, from)).get();
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

    /**
     * trouvé ici: https://stackoverflow.com/questions/5207163/how-to-do-myclassstring-class-in-java
     * @param tClass
     * @param <T2>
     * @return
     */
    @SuppressWarnings("unchecked")
    static public <T2> Class<List<T2>> listOf(Class<T2> tClass)
    {
        return (Class<List<T2>>)(Class<?>)(List.class);
    }

    public List<String> getModulesNames() {
        try {
            return queryGateway.send(new ModulesNamesQuery(), listOf(String.class)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
