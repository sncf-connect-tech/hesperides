package org.hesperides.infrastructure.postgresql.modules;

import org.hesperides.infrastructure.postgresql.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostgresqlModuleRepository extends JpaRepository<ModuleEntity, ModuleEntity.ModuleEntityId> {
}
