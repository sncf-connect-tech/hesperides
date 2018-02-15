package org.hesperides.application;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.domain.modules.commands.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.hesperides.domain.modules.queries.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Ensemble des cas d'utilisation liés à l'agrégat Module
 */
@Component
public class ModuleUseCases {

    private final CommandGateway commandGateway;

    private final QueryGateway queryGateway;

    public ModuleUseCases(CommandGateway commandGateway, QueryGateway queryGateway) {
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

    public void createTemplateInWorkingCopy(Module.Key key, Template template) throws Throwable {

        try {
            commandGateway.send(new CreateTemplateCommand(key, template)).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    public CompletableFuture<Object> updateTemplateInWorkingCopy(Module.Key key, Template template) throws Throwable {
        /**
         * TODO
         * Généraliser ce type d'appel pour éviter les try catch
         */
        return commandGateway.send(new UpdateTemplateCommand(key, template));
    }

    public void deleteTemplate(Module.Key key, String templateName) throws Throwable {
        try {
            commandGateway.send(new DeleteTemplateCommand(key, templateName)).get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    public CompletableFuture<Optional<ModuleView>> getModule(Module.Key moduleKey) {
        return queryGateway.send(new ModuleByIdQuery(moduleKey), optionalOf(ModuleView.class));
    }

    public CompletableFuture<List<String>> getModulesNames() {
        return queryGateway.send(new ModulesNamesQuery(), listOf(String.class));
    }

    public CompletableFuture<Optional<TemplateView>> getTemplate(Module.Key moduleKey, String templateName) {
        return queryGateway.send(new TemplateByNameQuery(moduleKey, templateName), optionalOf(TemplateView.class));
    }

    @SuppressWarnings("unchecked")
    static public <T2> Class<Optional<T2>> optionalOf(Class<T2> tClass) {
        return (Class<Optional<T2>>) (Class<?>) (Optional.class);
    }

    /**
     * Trouvé ici: https://stackoverflow.com/questions/5207163/how-to-do-myclassstring-class-in-java
     *
     * @param tClass
     * @param <T2>
     * @return
     */
    @SuppressWarnings("unchecked")
    static public <T2> Class<List<T2>> listOf(Class<T2> tClass) {
        return (Class<List<T2>>) (Class<?>) (List.class);
    }


}
