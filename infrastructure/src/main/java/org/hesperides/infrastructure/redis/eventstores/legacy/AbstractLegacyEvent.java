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
package org.hesperides.infrastructure.redis.eventstores.legacy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hesperides.domain.security.UserEvent;

public abstract class AbstractLegacyEvent {
    /**
     * Instance Gson spécifique à la sérialisation des évènements du domaine vers ceux de l'application existante
     */
    protected static Gson LEGACY_GSON_SERIALIZER = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    protected abstract UserEvent toDomainEvent(final String username);
}
