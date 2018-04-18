package org.hesperides.infrastructure.jpa.modules;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaModuleRepository extends JpaRepository<ModuleEntity, ModuleEntity.ModuleEntityId> {
}
