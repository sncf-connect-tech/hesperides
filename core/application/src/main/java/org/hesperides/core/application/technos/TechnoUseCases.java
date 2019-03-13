package org.hesperides.core.application.technos;

import org.hesperides.commons.spring.HasProfile;
import org.hesperides.core.domain.modules.exceptions.DuplicateModuleException;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.modules.queries.TechnoModuleView;
import org.hesperides.core.domain.security.User;
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

    private final TechnoCommands commands;
    private final TechnoQueries queries;
    private final ModuleQueries moduleQueries;

    @Autowired
    public TechnoUseCases(TechnoCommands commands, TechnoQueries queries, ModuleQueries moduleQueries) {
        this.commands = commands;
        this.queries = queries;
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

        String technoId = queries.getOptionalTechnoId(technoKey)
                .orElseGet(() -> {
                    Techno techno = new Techno(technoKey, Collections.emptyList());
                    return commands.createTechno(techno, user);
                });
        commands.addTemplate(technoId, template, user);
    }

    public void deleteTechno(TemplateContainer.Key technoKey, User user) {
        Optional<String> technoId = queries.getOptionalTechnoId(technoKey);
        if (!technoId.isPresent()) {
            throw new TechnoNotFoundException(technoKey);
        }
        List<TechnoModuleView> technoModulesViews = moduleQueries.getModulesUsingTechno(technoId.get());
        if (!HasProfile.dataMigration() && !technoModulesViews.isEmpty()) {
            throw new UndeletableTechnoInUseException(technoKey, technoModulesViews);
        }
        commands.deleteTechno(technoId.get(), user);
    }

    public void updateTemplateInWorkingCopy(TemplateContainer.Key technoKey, Template template, User user) {
        Optional<String> technoId = queries.getOptionalTechnoId(technoKey);
        if (!technoId.isPresent()) {
            throw new TechnoNotFoundException(technoKey);
        }
        commands.updateTemplate(technoId.get(), template, user);
    }

    public void deleteTemplate(TemplateContainer.Key technoKey, String templateName, User user) {
        Optional<String> technoId = queries.getOptionalTechnoId(technoKey);
        if (!technoId.isPresent()) {
            throw new TechnoNotFoundException(technoKey);
        }
        commands.deleteTemplate(technoId.get(), templateName, user);
    }

    public Optional<TemplateView> getTemplate(TemplateContainer.Key technoKey, String templateName) {
        if (!queries.technoExists(technoKey)) {
            throw new TechnoNotFoundException(technoKey);
        }
        return queries.getTemplate(technoKey, templateName);
    }

    public List<TemplateView> getTemplates(TemplateContainer.Key technoKey) {
        List<TemplateView> templates = Collections.emptyList();
        if (queries.technoExists(technoKey)) {
            templates = queries.getTemplates(technoKey);
        }
        return templates;
    }

    public TechnoView releaseTechno(TemplateContainer.Key existingTechnoKey, User user) {
        TemplateContainer.Key newTechnoKey = new Techno.Key(existingTechnoKey.getName(), existingTechnoKey.getVersion(), TemplateContainer.VersionType.release);
        if (queries.technoExists(newTechnoKey)) {
            throw new DuplicateTechnoException(newTechnoKey);
        }

        Optional<TechnoView> optionalTechnoView = queries.getOptionalTechno(existingTechnoKey);
        if (!optionalTechnoView.isPresent()) {
            throw new TechnoNotFoundException(existingTechnoKey);
        }

        Techno existingTechno = optionalTechnoView.get().toDomainInstance();
        Techno technoRelease = new Techno(newTechnoKey, existingTechno.getTemplates());

        commands.createTechno(technoRelease, user);
        return queries.getOptionalTechno(newTechnoKey).get();
    }

    public Optional<TechnoView> getTechno(TemplateContainer.Key technoKey) {
        return queries.getOptionalTechno(technoKey);
    }

    public List<String> getTechnosName() {
        return queries.getTechnosName();
    }

    public List<String> getTechnoVersions(String technoName) {
        return queries.getTechnoVersions(technoName);
    }

    public List<String> getTechnoTypes(String technoName, String technoVersion) {
        return queries.getTechnoTypes(technoName, technoVersion);
    }

    public List<TechnoView> search(String input) {
        return queries.search(input);
    }

    public TechnoView createWorkingCopyFrom(TemplateContainer.Key existingTechnoKey, TemplateContainer.Key newTechnoKey, User user) {
        if (queries.technoExists(newTechnoKey)) {
            throw new DuplicateModuleException(newTechnoKey);
        }

        Optional<TechnoView> optionalTechnoView = queries.getOptionalTechno(existingTechnoKey);
        if (!optionalTechnoView.isPresent()) {
            throw new ModuleNotFoundException(existingTechnoKey);
        }

        Techno existingTechno = optionalTechnoView.get().toDomainInstance();
        Techno newTechno = new Techno(newTechnoKey, existingTechno.getTemplates());

        commands.createTechno(newTechno, user);
        return queries.getOptionalTechno(newTechnoKey).get();
    }

    public List<AbstractPropertyView> getProperties(TemplateContainer.Key technoKey) {
        List<AbstractPropertyView> properties = Collections.emptyList();
        if (queries.technoExists(technoKey)) {
            properties = queries.getProperties(technoKey);
        }
        return properties;
    }
}
