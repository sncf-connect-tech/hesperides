package org.hesperides.core.domain.technos.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.commons.axon.AxonQueries;
import org.hesperides.core.domain.technos.*;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TechnoQueries extends AxonQueries {
    protected TechnoQueries(QueryGateway queryGateway) {
        super(queryGateway);
    }

    public Optional<String> getOptionalTechnoId(TemplateContainer.Key technoKey) {
        return querySyncOptional(new GetTechnoIdFromKeyQuery(technoKey), String.class);
    }

    public Boolean technoExists(TemplateContainer.Key technoKey) {
        return querySync(new TechnoExistsQuery(technoKey), Boolean.class);
    }

    public Optional<TemplateView> getTemplate(TemplateContainer.Key technoKey, String templateName) {
        return querySyncOptional(new GetTemplateQuery(technoKey, templateName), TemplateView.class);
    }

    public List<TemplateView> getTemplates(TemplateContainer.Key technoKey) {
        return querySyncList(new GetTemplatesQuery(technoKey), TemplateView.class);
    }

    public List<String> getTechnosName() {
        return querySyncList(new GetTechnosNameQuery(), String.class);
    }

    public List<String> getTechnoVersions(String technoName) {
        return querySyncList(new GetTechnoVersionsQuery(technoName), String.class);
    }

    public List<String> getTechnoTypes(String technoName, String technoVersion) {
        return querySyncList(new GetTechnoVersionTypesQuery(technoName, technoVersion), String.class);
    }

    public Optional<TechnoView> getOptionalTechno(TemplateContainer.Key technoKey) {
        return querySyncOptional(new GetTechnoQuery(technoKey), TechnoView.class);
    }

    public List<TechnoView> search(String input, int size) {
        return querySyncList(new SearchTechnosQuery(input, size), TechnoView.class);
    }

    public List<AbstractPropertyView> getProperties(TemplateContainer.Key technoKey) {
        return querySyncList(new GetTechnoPropertiesQuery(technoKey), AbstractPropertyView.class);
    }
}
