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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = RegressionTest.NRContextConfiguration.class)
public class RegressionTest {

    @ComponentScan({"org.hesperides.test.regression"})
    static class NRContextConfiguration {
    }

    @Autowired
    TechnosValidation technosValidation;
    @Autowired
    ModulesValidation modulesValidation;
    @Autowired
    PlatformsValidation platformsValidation;
    @Autowired
    private RegressionLogs regressionLogs;
    @Autowired
    private RegressionConfiguration regressionConfiguration;

    @Test
    public void launch() {
        if (regressionConfiguration.activateTests()) {

            technosValidation.validate();
//            modulesValidation.validate();
//            platformsValidation.validate();

            if (regressionLogs.hasDiffsOrException()) {
                regressionLogs.logDiffs();
                regressionLogs.logExceptions();
                regressionLogs.logStats();
                Assert.fail();
            } else {
                regressionLogs.logSuccess();
            }
        }
    }
}