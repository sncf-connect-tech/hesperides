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
package org.hesperides.core.application.platforms;

import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.junit.Test;

import java.util.Collections;

import static org.hesperides.core.application.platforms.PlatformUseCases.getPropertiesVersionId;
import static org.junit.Assert.assertEquals;

public class PlatformUseCasesTest {

    @Test
    public void testGetPropertiesVersionId() {
        PlatformView platformWithoutGlobalPropertiesVersionId = new PlatformView(null, null, null, null, false, null, null, null, null);
        assertEquals(Long.valueOf(0), getPropertiesVersionId(platformWithoutGlobalPropertiesVersionId, "#"));
        PlatformView platformWithGlobalPropertiesVersionId = new PlatformView(null, null, null, null, false, null, null, 1L, null);
        assertEquals(Long.valueOf(1), getPropertiesVersionId(platformWithGlobalPropertiesVersionId, "#"));

        DeployedModuleView deployedModuleWithoutPropertiesVersionId = new DeployedModuleView(1L, null, null, null, false, null, "A", null, null, null);
        PlatformView platform = new PlatformView(null, null, null, null, false, Collections.singletonList(deployedModuleWithoutPropertiesVersionId), null, null, null);
        assertEquals(Long.valueOf(0), getPropertiesVersionId(platform, "A"));

        DeployedModuleView deployedModuleWithPropertiesVersionId = new DeployedModuleView(1L, 1L, null, null, false, null, "A", null, null, null);
        platform = new PlatformView(null, null, null, null, false, Collections.singletonList(deployedModuleWithPropertiesVersionId), null, null, null);
        assertEquals(Long.valueOf(1), getPropertiesVersionId(platform, "A"));
    }
}
