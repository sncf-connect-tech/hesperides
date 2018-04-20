package org.hesperides.application;

import org.hesperides.domain.security.User;
import org.hesperides.domain.technos.commands.TechnoCommands;
import org.hesperides.domain.technos.queries.TechnoQueries;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Ensemble des cas d'utilisation liés à l'agrégat Techno
 */
@Component
public class TechnoUseCases {

    private final TechnoCommands commands;
    private final TechnoQueries queries;

    public TechnoUseCases(TechnoCommands commands, TechnoQueries queries) {
        this.commands = commands;
        this.queries = queries;
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
            commands.createTechno(technoKey, user);
        }
        commands.addTemplate(technoKey, template, user);
    }

    public Optional<TemplateView> getTemplate(TemplateContainer.Key technoKey, String templateName) {
        return queries.getTemplate(technoKey, templateName);
    }
}
