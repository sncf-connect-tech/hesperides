package org.hesperides.core.application.workshopproperties;

import org.hesperides.core.domain.security.User;
import org.hesperides.core.domain.workshopproperties.commands.WorkshopPropertyCommands;
import org.hesperides.core.domain.workshopproperties.entities.WorkshopProperty;
import org.hesperides.core.domain.workshopproperties.exceptions.DuplicateWorkshopPropertyException;
import org.hesperides.core.domain.workshopproperties.exceptions.WorkshopPropertyNotFoundException;
import org.hesperides.core.domain.workshopproperties.queries.WorkshopPropertyQueries;
import org.hesperides.core.domain.workshopproperties.queries.views.WorkshopPropertyView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkshopPropertyUseCases {

    private final WorkshopPropertyCommands commands;
    private final WorkshopPropertyQueries queries;

    @Autowired
    public WorkshopPropertyUseCases(WorkshopPropertyCommands commands, WorkshopPropertyQueries queries) {
        this.commands = commands;
        this.queries = queries;
    }

    public String createWorkshopProperty(WorkshopProperty workshopProperty, User user) {
        if (queries.workshopPropertyExists(workshopProperty.getKey())) {
            throw new DuplicateWorkshopPropertyException(workshopProperty.getKey());
        }
        return commands.createWorkshopProperty(workshopProperty, user);
    }

    public WorkshopPropertyView getWorkshopProperty(String workshopPropertyKey) {
        if (!queries.workshopPropertyExists(workshopPropertyKey)) {
            throw new WorkshopPropertyNotFoundException(workshopPropertyKey);
        }
        return queries.getWorkshopProperty(workshopPropertyKey);
    }

    public void updateWorkshopProperty(WorkshopProperty workshopProperty, User user) {
        if (!queries.workshopPropertyExists(workshopProperty.getKey())) {
            throw new WorkshopPropertyNotFoundException(workshopProperty.getKey());
        }
        commands.updateWorkshopProperty(workshopProperty, user);
    }
}
