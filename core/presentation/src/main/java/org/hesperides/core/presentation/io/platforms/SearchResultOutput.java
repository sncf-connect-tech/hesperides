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
package org.hesperides.core.presentation.io.platforms;

import lombok.Value;
import org.hesperides.core.domain.platforms.queries.views.SearchApplicationResultView;
import org.hesperides.core.domain.platforms.queries.views.SearchPlatformResultView;

@Value
public class SearchResultOutput {

    String name;

    public SearchResultOutput(SearchPlatformResultView view) {
        this.name = view.getPlatformName();
    }

    public SearchResultOutput(SearchApplicationResultView view) {
        this.name = view.getApplicationName();
    }
}
