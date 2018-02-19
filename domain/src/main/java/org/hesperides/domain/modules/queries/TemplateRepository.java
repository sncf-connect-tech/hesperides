package org.hesperides.domain.modules.queries;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.TemplateByNameQuery;

import java.util.Optional;

/**
 * stock et query les templates
 */
public interface TemplateRepository {
    @QueryHandler
    Optional<TemplateView> queryTemplateByName(TemplateByNameQuery query);
}
