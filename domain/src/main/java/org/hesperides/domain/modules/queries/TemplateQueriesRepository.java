package org.hesperides.domain.modules.queries;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.GetTemplateByNameQuery;
import org.hesperides.domain.templatecontainer.queries.TemplateView;

import java.util.Optional;

public interface TemplateQueriesRepository {
    @QueryHandler
    Optional<TemplateView> query(GetTemplateByNameQuery query);
}
