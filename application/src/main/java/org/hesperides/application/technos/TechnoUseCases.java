package org.hesperides.application.technos;

import com.github.mustachejava.Code;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.hesperides.domain.modules.exceptions.DuplicateModuleException;
import org.hesperides.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.domain.security.User;
import org.hesperides.domain.technos.commands.TechnoCommands;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.technos.exception.DuplicateTechnoException;
import org.hesperides.domain.technos.exception.TechnoNotFoundException;
import org.hesperides.domain.technos.queries.TechnoQueries;
import org.hesperides.domain.technos.queries.TechnoView;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.PropertiesModel;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.ArrayList;
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
    private final MustacheFactory mustacheFactory;

    @Autowired
    public TechnoUseCases(TechnoCommands commands, TechnoQueries queries, MustacheFactory mustacheFactory) {
        this.commands = commands;
        this.queries = queries;
        this.mustacheFactory = mustacheFactory;
    }

    /**
     * Crée la techno si elle n'existe pas
     * Ajoute un template à cette techno
     *
     * @param technoKey
     * @param template
     * @param user
     */
    public void addTemplate(TemplateContainer.Key technoKey, Template template, User user) {
        if (!queries.technoExists(technoKey)) {
            Techno techno = new Techno(technoKey, Collections.emptyList());
            commands.createTechno(techno, user);
        }
        commands.addTemplate(technoKey, template, user);
    }

    public void updateTemplateInWorkingCopy(Techno.Key technoKey, Template template, User user) {
        commands.updateTemplate(technoKey, template, user);
    }

    public void deleteTemplate(Techno.Key technoKey, String templateName, User user) {
        commands.deleteTemplate(technoKey, templateName, user);
    }

    public Optional<TemplateView> getTemplate(TemplateContainer.Key technoKey, String templateName) {
        return queries.getTemplate(technoKey, templateName);
    }

    public void deleteTechno(TemplateContainer.Key technoKey, User user) {
        if (!queries.technoExists(technoKey)) {
            throw new TechnoNotFoundException(technoKey);
        }
        commands.deleteTechno(technoKey, user);
    }

    public List<TemplateView> getTemplates(TemplateContainer.Key technoKey) {
        if (!queries.technoExists(technoKey)) {
            throw new TechnoNotFoundException(technoKey);
        }
        return queries.getTemplates(technoKey);
    }

    public TechnoView releaseTechno(TemplateContainer.Key existingTechnoKey, User user) {
        TemplateContainer.Key newTechnoKey = new TemplateContainer.Key(existingTechnoKey.getName(), existingTechnoKey.getVersion(), TemplateContainer.VersionType.release);
        if (queries.technoExists(newTechnoKey)) {
            throw new DuplicateTechnoException(newTechnoKey);
        }

        Optional<TechnoView> technoView = queries.getTechno(existingTechnoKey);
        if (!technoView.isPresent()) {
            throw new TechnoNotFoundException(existingTechnoKey);
        }

        Techno existingTechno = technoView.get().toDomainInstance();
        Techno technoRelease = new Techno(newTechnoKey, existingTechno.getTemplates());

        commands.createTechno(technoRelease, user);
        return queries.getTechno(newTechnoKey).get();
    }

    public List<TechnoView> search(String input) {
        return queries.search(input);
    }

    public TechnoView createWorkingCopyFrom(TemplateContainer.Key existingTechnoKey, TemplateContainer.Key newTechnoKey, User user) {
        if (queries.technoExists(newTechnoKey)) {
            throw new DuplicateModuleException(newTechnoKey);
        }

        Optional<TechnoView> technoView = queries.getTechno(existingTechnoKey);
        if (!technoView.isPresent()) {
            throw new ModuleNotFoundException(existingTechnoKey);
        }

        Techno existingTechno = technoView.get().toDomainInstance();
        Techno newTechno = new Techno(newTechnoKey, existingTechno.getTemplates());

        commands.createTechno(newTechno, user);
        return queries.getTechno(newTechnoKey).get();
    }

    public PropertiesModel getPropertiesModel(TemplateContainer.Key technoKey) {
        List<PropertiesModel.KeyValueProperty> keyValueProperties = new ArrayList<>();
        List<PropertiesModel.IterableProperty> iterableProperties = new ArrayList<>();

        /**
         * Récupérer les templates de la techno
         * Pour chaque template, extraire les propriétés
         * Et les fusionner
         */

        List<TemplateView> templateViews = getTemplates(technoKey);
        if (templateViews != null) {
            for (TemplateView templateView : templateViews) {
                Mustache mustache = mustacheFactory.compile(new StringReader(templateView.getContent()), "something");
                for (Code code : mustache.getCodes()) {
//                    code.
                }
            }
        }

        return new PropertiesModel(keyValueProperties, iterableProperties);
    }
}
