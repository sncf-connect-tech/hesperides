package org.hesperides.domain.modules.queries;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.GetModuleTemplatesQuery;
import org.hesperides.domain.modules.GetTemplateByNameQuery;
import org.hesperides.domain.templatecontainer.queries.TemplateView;

import java.util.List;
import java.util.Optional;

public interface TemplateQueriesRepository {

    @QueryHandler
    Optional<TemplateView> query(GetTemplateByNameQuery query);

    @QueryHandler
    List<TemplateView> query(GetModuleTemplatesQuery query);
}
