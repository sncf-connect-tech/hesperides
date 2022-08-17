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
package org.hesperides.test.regression;

import org.hesperides.test.regression.validation.ModulesValidation;
import org.hesperides.test.regression.validation.PlatformsValidation;
import org.hesperides.test.regression.validation.TechnosValidation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = RegressionTests.NRContextConfiguration.class,
        initializers = ConfigDataApplicationContextInitializer.class)
public class RegressionTests {

    @Autowired
    TechnosValidation technosValidation;
    @Autowired
    ModulesValidation modulesValidation;
    @Autowired
    PlatformsValidation platformsValidation;
    @Value("${regressionTest.validate.technos}")
    private boolean validateTechnos;
    @Value("${regressionTest.validate.modules}")
    private boolean validateModules;
    @Value("${regressionTest.validate.platforms}")
    private boolean validatePlatforms;
    @Autowired
    private RegressionLogs regressionLogs;

    @Test
    public void launch() {
        if (validateTechnos) {
            technosValidation.validate();
        }
        if (validateModules) {
            modulesValidation.validate();
        }
        if (validatePlatforms) {
            platformsValidation.validate();
        }

        if (regressionLogs.hasDiffsOrExceptions()) {
            regressionLogs.logDiffs();
            regressionLogs.logExceptions();
            regressionLogs.logStats();
            Assert.fail();
        } else {
            regressionLogs.logSuccess();
        }
    }

    @ComponentScan({"org.hesperides.test.regression"})
    static class NRContextConfiguration {
    }
}
