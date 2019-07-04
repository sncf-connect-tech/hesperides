package org.hesperides.core.application.technos;

import org.hesperides.core.domain.modules.exceptions.DuplicateModuleException;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.technos.commands.TechnoCommands;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.technos.exception.DuplicateTechnoException;
import org.hesperides.core.domain.technos.exception.TechnoNotFoundException;
import org.hesperides.core.domain.technos.exception.UndeletableTechnoInUseException;
import org.hesperides.core.domain.technos.queries.TechnoQueries;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.KeyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Ensemble des cas d'utilisation liés à l'agrégat Techno
 */
@Component
public class TechnoUseCases {

    private static final int DEFAULT_NB_SEARCH_RESULTS = 10;

    private final TechnoCommands technoCommands;
    private final TechnoQueries technoQueries;
    private final ModuleQueries moduleQueries;

    @Autowired
    public TechnoUseCases(TechnoCommands technoCommands, TechnoQueries technoQueries, ModuleQueries moduleQueries) {
        this.technoCommands = technoCommands;
        this.technoQueries = technoQueries;
        this.moduleQueries = moduleQueries;
    }

    /**
     * Crée la techno si elle n'existe pas.
     * Ajoute le template à cette techno.
     * 
     * @param technoKey
     * @param template
     * @param user
     */
    public void addTemplate(TemplateContainer.Key technoKey, Template template, User user) {

        String technoId = technoQueries.getOptionalTechnoId(technoKey)
                .orElseGet(() -> {
                    Techno techno = new Techno(technoKey, Collections.emptyList());
                    return technoCommands.createTechno(techno, user);
                });
        technoCommands.addTemplate(technoId, template, user);
    }

    public void deleteTechno(TemplateContainer.Key technoKey, User user) {
        Optional<String> technoId = technoQueries.getOptionalTechnoId(technoKey);
        if (!technoId.isPresent()) {
            throw new TechnoNotFoundException(technoKey);
        }
        List<KeyView> technoModuleKeys = moduleQueries.getModulesUsingTechno(technoId.get());
        if (!technoModuleKeys.isEmpty()) {
            throw new UndeletableTechnoInUseException(technoKey, technoModuleKeys);
        }
        technoCommands.deleteTechno(technoId.get(), user);
    }

    public void updateTemplateInWorkingCopy(TemplateContainer.Key technoKey, Template template, User user) {
        Optional<String> technoId = technoQueries.getOptionalTechnoId(technoKey);
        if (!technoId.isPresent()) {
            throw new TechnoNotFoundException(technoKey);
        }
        technoCommands.updateTemplate(technoId.get(), template, user);
    }

    public void deleteTemplate(TemplateContainer.Key technoKey, String templateName, User user) {
        Optional<String> technoId = technoQueries.getOptionalTechnoId(technoKey);
        if (!technoId.isPresent()) {
            throw new TechnoNotFoundException(technoKey);
        }
        technoCommands.deleteTemplate(technoId.get(), templateName, user);
    }

    public Optional<TemplateView> getTemplate(TemplateContainer.Key technoKey, String templateName) {
        if (!technoQueries.technoExists(technoKey)) {
            throw new TechnoNotFoundException(technoKey);
        }
        return technoQueries.getTemplate(technoKey, templateName);
    }

    public List<TemplateView> getTemplates(TemplateContainer.Key technoKey) {
        List<TemplateView> templates = Collections.emptyList();
        if (technoQueries.technoExists(technoKey)) {
            templates = technoQueries.getTemplates(technoKey);
        }
        return templates;
    }

    public TechnoView releaseTechno(TemplateContainer.Key existingTechnoKey, User user) {
        TemplateContainer.Key newTechnoKey = new Techno.Key(existingTechnoKey.getName(), existingTechnoKey.getVersion(), TemplateContainer.VersionType.release);
        if (technoQueries.technoExists(newTechnoKey)) {
            throw new DuplicateTechnoException(newTechnoKey);
        }

        Optional<TechnoView> optionalTechnoView = technoQueries.getOptionalTechno(existingTechnoKey);
        if (!optionalTechnoView.isPresent()) {
            throw new TechnoNotFoundException(existingTechnoKey);
        }

        Techno existingTechno = optionalTechnoView.get().toDomainInstance();
        Techno technoRelease = new Techno(newTechnoKey, existingTechno.getTemplates());

        technoCommands.createTechno(technoRelease, user);
        return technoQueries.getOptionalTechno(newTechnoKey).get();
    }

    public Optional<TechnoView> getTechno(TemplateContainer.Key technoKey) {
        return technoQueries.getOptionalTechno(technoKey);
    }

    public List<String> getTechnosName() {
        return technoQueries.getTechnosName();
    }

    public List<String> getTechnoVersions(String technoName) {
        return technoQueries.getTechnoVersions(technoName);
    }

    public List<String> getTechnoTypes(String technoName, String technoVersion) {
        return technoQueries.getTechnoTypes(technoName, technoVersion);
    }

    public List<TechnoView> search(String input, Integer providedSize) {
        int size = providedSize != null && providedSize > 0 ? providedSize : DEFAULT_NB_SEARCH_RESULTS;
        return technoQueries.search(input, size);
    }

    public TechnoView createWorkingCopyFrom(TemplateContainer.Key existingTechnoKey, TemplateContainer.Key newTechnoKey, User user) {
        if (technoQueries.technoExists(newTechnoKey)) {
            throw new DuplicateModuleException(newTechnoKey);
        }

        Optional<TechnoView> optionalTechnoView = technoQueries.getOptionalTechno(existingTechnoKey);
        if (!optionalTechnoView.isPresent()) {
            throw new ModuleNotFoundException(existingTechnoKey);
        }

        Techno existingTechno = optionalTechnoView.get().toDomainInstance();
        Techno newTechno = new Techno(newTechnoKey, existingTechno.getTemplates());

        technoCommands.createTechno(newTechno, user);
        return technoQueries.getOptionalTechno(newTechnoKey).get();
    }

    public List<AbstractPropertyView> getProperties(TemplateContainer.Key technoKey) {
        List<AbstractPropertyView> properties = Collections.emptyList();
        if (technoQueries.technoExists(technoKey)) {
            properties = technoQueries.getProperties(technoKey);
        }
        return properties;
    }
}
