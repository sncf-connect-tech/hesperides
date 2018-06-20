package org.hesperides.domain.technos.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.domain.framework.Queries;
import org.hesperides.domain.technos.*;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.domain.templatecontainers.queries.TemplateView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TechnoQueries extends Queries {
    protected TechnoQueries(QueryGateway queryGateway) {
        super(queryGateway);
    }

    public Boolean technoExists(TemplateContainer.Key technoKey) {
        return querySync(new TechnoAlreadyExistsQuery(technoKey), Boolean.class);
    }

    public Optional<TemplateView> getTemplate(TemplateContainer.Key technoKey, String templateName) {
        return querySyncOptional(new GetTemplateQuery(technoKey, templateName), TemplateView.class);
    }

    public List<TemplateView> getTemplates(TemplateContainer.Key technoKey) {
        return querySyncList(new GetTemplatesQuery(technoKey), TemplateView.class);
    }

    public Optional<TechnoView> getTechno(TemplateContainer.Key technoKey) {
        return querySyncOptional(new GetTechnoQuery(technoKey), TechnoView.class);
    }

    public List<TechnoView> search(String input) {
        return querySyncList(new SearchTechnosQuery(input), TechnoView.class);
    }

    public List<AbstractPropertyView> getProperties(TemplateContainer.Key technoKey) {
        return querySyncList(new GetTechnoPropertiesQuery(technoKey), AbstractPropertyView.class);
    }
}
