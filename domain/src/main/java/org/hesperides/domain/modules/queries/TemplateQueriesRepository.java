package org.hesperides.domain.modules.queries;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.TemplateByNameQuery;
import org.hesperides.domain.templatecontainer.queries.TemplateView;

import java.util.Optional;

/**
 * stock et query les templates
 */
public interface TemplateQueriesRepository {
    @QueryHandler
    Optional<TemplateView> queryTemplateByName(TemplateByNameQuery query);
}
