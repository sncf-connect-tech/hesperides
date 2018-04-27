package org.hesperides.infrastructure.mongo.technos;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public interface MongoTechnoRepository extends MongoRepository<TechnoDocument, String> {

    Optional<TechnoDocument> findOptionalByNameAndVersionAndWorkingCopy(String name, String version, boolean isWorkingCopy);

    TechnoDocument findByNameAndVersionAndWorkingCopy(String name, String version, boolean isWorkingCopy);

    TechnoDocument findByNameAndVersionAndWorkingCopyAndTemplatesName(String name, String version, boolean isWorkingCopy, String templateName);
}
