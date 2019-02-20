package org.hesperides.core.infrastructure.inmemory.platforms;

import org.hesperides.core.infrastructure.mongo.platforms.MongoPlatformRepository;
import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformDocument;
import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformKeyDocument;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public class InmemoryPlatformRepository implements MongoPlatformRepository {

    private PlatformDocument currentPlatformDocument = null;

    public PlatformDocument getCurrentPlatformDocument() {
        return currentPlatformDocument;
    }

    @Override
    public PlatformDocument save(PlatformDocument platformDoc) {
        currentPlatformDocument = platformDoc;
        return platformDoc;
    }

    @Override
    public Optional<PlatformDocument> findById(String id) {
        if (currentPlatformDocument == null || !id.equals(currentPlatformDocument.getId())) {
            throw new UnsupportedOperationException();
        }
        return Optional.of(currentPlatformDocument);
    }

    @Override
    public void deleteById(String id) {
        if (currentPlatformDocument == null || !id.equals(currentPlatformDocument.getId())) {
            throw new UnsupportedOperationException();
        }
        currentPlatformDocument = null;
    }

    /****************** DUMMY methods: *******************/

    @Override
    public Optional<PlatformDocument> findOptionalIdByKey(PlatformKeyDocument key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean existsByKey(PlatformKeyDocument platformKeyDocument) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<PlatformDocument> findOptionalByKey(PlatformKeyDocument platformKeyDocument) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PlatformDocument> findAllByKeyApplicationName(String applicationName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PlatformDocument> findAllByDeployedModulesNameAndDeployedModulesVersionAndDeployedModulesIsWorkingCopy(
            String moduleName, String moduleVersion, boolean isWorkingCopy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PlatformDocument> findAllByKeyApplicationNameLike(String input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PlatformDocument> findAllByKeyApplicationNameLikeAndKeyPlatformNameLike(String applicationName, String platformName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<PlatformDocument> findModuleByPropertiesPath(PlatformKeyDocument platformKeyDocument, String propertiesPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean existsByPlatformKeyAndModuleKeyAndPath(PlatformKeyDocument platformKeyDocument, String moduleName, String moduleVersion, boolean isWorkingCopy, String modulePath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean existsByPlatformKeyAndModuleKeyAndPathAndInstanceName(PlatformKeyDocument platformKeyDocument, String moduleName, String moduleVersion, boolean isWorkingCopy, String modulePath, String instanceName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<PlatformDocument> findGlobalPropertiesByPlatformKey(PlatformKeyDocument platformKeyDocument) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends PlatformDocument> List<S> saveAll(Iterable<S> iterable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean existsById(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PlatformDocument> findAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<PlatformDocument> findAllById(Iterable<String> iterable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(PlatformDocument platformDocument) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll(Iterable<? extends PlatformDocument> iterable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PlatformDocument> findAll(Sort sort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Page<PlatformDocument> findAll(Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlatformDocument insert(PlatformDocument s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends PlatformDocument> List<S> insert(Iterable<S> iterable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends PlatformDocument> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends PlatformDocument> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends PlatformDocument> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends PlatformDocument> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends PlatformDocument> long count(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends PlatformDocument> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException();
    }
}