package org.hesperides.domain.technos.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.domain.framework.Queries;
import org.hesperides.domain.technos.GetTemplateQuery;
import org.hesperides.domain.technos.TechnoAlreadyExistsQuery;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TechnoQueries extends Queries {
    protected TechnoQueries(QueryGateway queryGateway) {
        super(queryGateway);
    }

    public Boolean technoExists(TemplateContainer.Key newTechnoKey) {
        return querySync(new TechnoAlreadyExistsQuery(newTechnoKey), Boolean.class);
    }

    public Optional<TemplateView> getTemplate(TemplateContainer.Key technoKey, String templateName) {
        return querySyncOptional(new GetTemplateQuery(technoKey, templateName), TemplateView.class);
    }
}
