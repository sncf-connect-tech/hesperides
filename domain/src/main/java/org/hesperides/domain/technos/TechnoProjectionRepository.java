/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.domain.technos;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.technos.queries.TechnoView;
import org.hesperides.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.domain.templatecontainers.queries.TemplateView;

import java.util.List;
import java.util.Optional;

public interface TechnoProjectionRepository {

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    void on(TechnoCreatedEvent event);

    @EventSourcingHandler
    void on(TechnoDeletedEvent event);

    @EventSourcingHandler
    void on(TemplateAddedToTechnoEvent event);

    @EventSourcingHandler
    void on(TechnoTemplateUpdatedEvent event);

    @EventSourcingHandler
    void on(TechnoTemplateDeletedEvent event);

    /*** QUERY HANDLERS ***/

    @QueryHandler
    Optional<TemplateView> query(GetTemplateQuery query);

    @QueryHandler
    Boolean query(TechnoAlreadyExistsQuery query);

    @QueryHandler
    List<TemplateView> query(GetTemplatesQuery query);

    @QueryHandler
    Optional<TechnoView> query(GetTechnoQuery query);

    @QueryHandler
    List<TechnoView> query(SearchTechnosQuery query);

    @QueryHandler
    List<AbstractPropertyView> query(GetTechnoPropertiesQuery query);
}
