package org.hesperides.domain.modules;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.queries.TemplateByNameQuery;
import org.hesperides.domain.modules.queries.TemplateView;

import java.util.Optional;

/**
 * stock et query les templates
 */
public interface TemplatesRepository {
    @QueryHandler
    Optional<TemplateView> queryTemplateByName(TemplateByNameQuery query);
}
