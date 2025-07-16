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
package org.hesperides.core.domain.platforms.queries.views;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.security.queries.views.ApplicationDirectoryGroupsView;

import java.util.List;
import java.util.Set;

@Value
@AllArgsConstructor
public class ApplicationView {
    String name;
    List<PlatformView> platforms;
    ApplicationDirectoryGroupsView directoryGroups;

    public ApplicationView(String name, List<PlatformView> platforms) {
        this.name = name;
        this.platforms = platforms;
        this.directoryGroups = null;
    }

    public ApplicationView withDirectoryGoups(ApplicationDirectoryGroupsView applicationDirectoryGroups) {
        return new ApplicationView(name, platforms, applicationDirectoryGroups);
    }

    public ApplicationView withPasswordIndicator(Set<Platform.Key> platformsWithPassword) {
        return new ApplicationView(name, PlatformView.setPlatformsWithPasswordIndicator(platforms, platformsWithPassword), directoryGroups);
    }
}
