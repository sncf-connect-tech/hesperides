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
package org.hesperides.test.nr;

import lombok.extern.slf4j.Slf4j;
import org.hesperides.test.nr.validation.ModulesValidation;
import org.hesperides.test.nr.validation.PlatformsValidation;
import org.hesperides.test.nr.validation.TechnosValidation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {NRConfiguration.class})
@Slf4j
public class NRTest {

    @Autowired
    TechnosValidation technosValidation;
    @Autowired
    ModulesValidation modulesValidation;
    @Autowired
    PlatformsValidation platformsValidation;

    @Test
    public void launch() {
        technosValidation.validate();
        modulesValidation.validate();
        platformsValidation.validate();

        //TODO Logs, stats et logs par entité

//        logDiffs();
//        logUnexpectedExceptions();
    }
}