package org.hesperides.application;

import org.hesperides.domain.security.User;
import org.hesperides.domain.technos.commands.TechnoCommands;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.technos.queries.TechnoQueries;
import org.springframework.stereotype.Component;

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
     * creer une working copy, vérifie que le techno n'existe pas déjà.
     * <p>
     * On test si le techno existe déjà ou pas dans cette couche car un aggregat (un techno)
     * n'as pas accès aux autres aggregats.
     *
     * @param techno
     * @param user
     * @return
     */
    public Techno.Key createWorkingCopy(Techno techno, User user) {
        /*if (queries.technoExist(techno.getKey())) {
            throw new DuplicateTechnoException(techno.getKey());
        }*/
        return commands.createTechno(techno, user);
    }
}
