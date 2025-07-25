/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/sncf-connect-tech/hesperides)
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
package org.hesperides.commons;

import java.util.Optional;

/**
 * Interface à utiliser lorsqu'un objet doit implémenter la méthode toDomainInstance sur un object potentiellement null.
 * "Primer" signifie "amorceur" (du verbe to prime, amorcer).
 *
 * @param <T>
 */
public interface DomainPrimer<T> {
    T toDomainInstance();

    static <O> O toDomainInstanceOrNull(DomainPrimer<O> candidate) {
        return Optional.ofNullable(candidate).map(DomainPrimer::toDomainInstance).orElse(null);
    }
}
