package org.hesperides.infrastructure.jpa.modules;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.queries.AbstractPropertyView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.domain.framework.Profiles.JPA;

@Profile(JPA)
@Component
public class JpaModuleProjectionRepository implements ModuleProjectionRepository {

    private final JpaModuleRepository jpaModuleRepository;

    @Autowired
    public JpaModuleProjectionRepository(JpaModuleRepository jpaModuleRepository) {
        this.jpaModuleRepository = jpaModuleRepository;
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleCreatedEvent event) {
        Module module = event.getModule();
        ModuleEntity.ModuleEntityId id = new ModuleEntity.ModuleEntityId(
                module.getKey().getName(),
                module.getKey().getVersion(),
                module.getKey().getVersionType()
        );
        jpaModuleRepository.save(
                new ModuleEntity(
                        id,
                        module.getVersionId()
                )
        );
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleTechnosUpdatedEvent event) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @EventSourcingHandler
    @Override
    public void on(ModuleDeletedEvent event) {
        TemplateContainer.Key key = event.getModuleKey();
        ModuleEntity.ModuleEntityId id = new ModuleEntity.ModuleEntityId(
                key.getName(),
                key.getVersion(),
                key.getVersionType()
        );
        jpaModuleRepository.delete(id);
    }

    @QueryHandler
    @Override
    public Optional<ModuleView> query(GetModuleByKeyQuery query) {
        Optional<ModuleView> optionalModuleView = Optional.empty();
        ModuleEntity.ModuleEntityId id = new ModuleEntity.ModuleEntityId(
                query.getModuleKey().getName(),
                query.getModuleKey().getVersion(),
                query.getModuleKey().getVersionType()
        );
        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity.setModuleEntityId(id);
        moduleEntity = jpaModuleRepository.findOne(id);
        if (moduleEntity != null) {
            optionalModuleView = Optional.of(moduleEntity.toModuleView());
        }
        return optionalModuleView;
    }

    @QueryHandler
    @Override
    public List<String> query(GetModulesNamesQuery query) {
        return jpaModuleRepository.findAll()
                .stream()
                .map(ModuleEntity::getModuleEntityId)
                .map(ModuleEntity.ModuleEntityId::getName)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> query(GetModuleVersionTypesQuery query) {
        ModuleEntity.ModuleEntityId id = new ModuleEntity.ModuleEntityId(
                query.getModuleName(),
                query.getModuleVersion(),
                null
        );
        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity.setModuleEntityId(id);
        return jpaModuleRepository.findAll(Example.of(moduleEntity))
                .stream()
                .map(ModuleEntity::getModuleEntityId)
                .map(ModuleEntity.ModuleEntityId::getVersionType)
                .map(TemplateContainer.VersionType::toString)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> query(GetModuleVersionsQuery query) {
        ModuleEntity.ModuleEntityId id = new ModuleEntity.ModuleEntityId(
                query.getModuleName(),
                null,
                null
        );
        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity.setModuleEntityId(id);
        return jpaModuleRepository.findAll(Example.of(moduleEntity))
                .stream()
                .map(ModuleEntity::getModuleEntityId)
                .map(ModuleEntity.ModuleEntityId::getVersion)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public Boolean query(ModuleAlreadyExistsQuery query) {
        ModuleEntity.ModuleEntityId id = new ModuleEntity.ModuleEntityId(
                query.getModuleKey().getName(),
                query.getModuleKey().getVersion(),
                query.getModuleKey().getVersionType()
        );
        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity.setModuleEntityId(id);
        return jpaModuleRepository.exists(Example.of(moduleEntity));
    }

    @Override
    @QueryHandler
    public List<ModuleView> query(SearchModulesQuery query) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<AbstractPropertyView> query(GetModulePropertiesQuery query) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
