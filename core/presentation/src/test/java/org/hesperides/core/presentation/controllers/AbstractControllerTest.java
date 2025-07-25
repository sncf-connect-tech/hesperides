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
package org.hesperides.core.presentation.controllers;

import org.hesperides.core.application.events.EventUseCases;
import org.hesperides.core.application.files.FileUseCases;
import org.hesperides.core.application.modules.ModuleUseCases;
import org.hesperides.core.application.platforms.PlatformUseCases;
import org.hesperides.core.application.platforms.PropertiesUseCases;
import org.hesperides.core.application.security.ApplicationDirectoryGroupsUseCases;
import org.hesperides.core.application.security.UserUseCases;
import org.hesperides.core.application.technos.TechnoUseCases;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

public abstract class AbstractControllerTest {
    @Autowired
    protected MockMvc mvc;
    @MockBean
    protected ModuleUseCases moduleUseCases;
    @MockBean
    protected TechnoUseCases technoUseCases;
    @MockBean
    protected PlatformUseCases platformUseCases;
    @MockBean
    protected PropertiesUseCases propertiesUseCases;
    @MockBean
    protected EventUseCases eventUseCases;
    @MockBean
    protected FileUseCases fileUseCases;
    @MockBean
    protected UserUseCases userUseCases;
    @MockBean
    protected ApplicationDirectoryGroupsUseCases applicationDirectoryGroupsUseCases;
}
