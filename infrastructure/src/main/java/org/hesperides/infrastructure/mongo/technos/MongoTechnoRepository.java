package org.hesperides.infrastructure.mongo.technos;

import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public interface MongoTechnoRepository extends MongoRepository<TechnoDocument, String> {
    TechnoDocument findByNameAndVersionAndVersionType(String name, String version, TemplateContainer.Type versionType);
}
