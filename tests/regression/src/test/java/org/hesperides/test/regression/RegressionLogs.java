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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.test.regression.errors.AbstractError;
import org.hesperides.test.regression.errors.Diff;
import org.hesperides.test.regression.errors.UnexpectedException;
import org.hesperides.test.regression.validation.ModulesValidation;
import org.hesperides.test.regression.validation.PlatformsValidation;
import org.hesperides.test.regression.validation.TechnosValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RegressionLogs {

    @Autowired
    private RegressionConfiguration regressionConfiguration;

    private List<Diff> diffs = new ArrayList<>();
    private List<UnexpectedException> exceptions = new ArrayList<>();

    public void logAndSaveDiff(Diff diff) {
        if (regressionConfiguration.logWhileTesting()) {
            logDiff(diff);
        }
        diffs.add(diff);
    }

    public void logAndSaveException(UnexpectedException exception) {
        if (regressionConfiguration.logWhileTesting()) {
            logException(exception);
        }
        exceptions.add(exception);
    }

    void logDiffs() {
        log.warn("*************** DIFFS ***************", diffs.size() > 0 ? "ALL" : "NO");
        log.warn("");
        logErrors(diffs, "{} diff(s) for entity \"{}\":");
    }

    void logExceptions() {
        log.warn("************ EXCEPTIONS ************", exceptions.size() > 0 ? "ALL" : "NO");
        log.warn("");
        logErrors(exceptions, "{} exception(s) while testing \"{}\":");
    }

    private void logErrors(List<? extends AbstractError> errors, String logMessage) {
        List<String> distinctKeys = getDistinctKeys(errors);
        distinctKeys.forEach(entityKey -> {
            List<AbstractError> entityDiffs = errors.stream().filter(error -> error.getEntityKey().equals(entityKey)).collect(Collectors.toList());
            log.warn(logMessage, entityDiffs.size(), entityKey);
            entityDiffs.forEach(this::logError);
        });
    }

    private List<String> getDistinctKeys(List<? extends AbstractError> errors) {
        return getDistinctKeys(errors, null);
    }

    private List<String> getDistinctKeys(List<? extends AbstractError> errors, String keyPrefix) {
        return errors.stream()
                .map(AbstractError::getEntityKey)
                .filter(entityKey -> StringUtils.isEmpty(keyPrefix) || entityKey.startsWith(keyPrefix))
                .distinct().sorted().collect(Collectors.toList());
    }

    private void logError(AbstractError error) {
        log.warn("");
        log.warn("Latest url: " + error.getLatestUri());
        log.warn("Testing url: " + error.getTestingUri());
        log.warn(error.getMessage());
        log.warn("");
    }

    private void logDiff(Diff diff) {
        log.warn("");
        log.warn("****** New diff for \"{}\" ******", diff.getEntityKey());
        logError(diff);
    }

    private void logException(UnexpectedException exception) {
        log.warn("");
        log.warn("****** New exception while testing \"{}\" ******", exception.getEntityKey());
        logError(exception);
    }

    void logStats() {
        log.warn("");
        log.warn("************** STATS ***************");
        log.warn("{} endpoint(s) with diff", diffs.size());
        log.warn("{} endpoint(s) with exception", exceptions.size());
        log.warn("{} entitie(s) with at least one diff", getDistinctKeys(diffs).size());
        log.warn("{} entitie(s) with at least one exception", getDistinctKeys(exceptions).size());
        log.warn("************************************");
        log.warn("{} techno(s) with at least one diffs", getDistinctKeys(diffs, TechnosValidation.TECHNO_KEY_PREFIX).size());
        log.warn("{} module(s) with at least one diffs", getDistinctKeys(diffs, ModulesValidation.MODULE_KEY_PREFIX).size());
        log.warn("{} platform(s) with at least one diffs", getDistinctKeys(diffs, PlatformsValidation.PLATFORM_KEY_PREFIX).size());
        log.warn("************************************");
        log.warn("");
    }

    boolean hasDiffsOrException() {
        return !CollectionUtils.isEmpty(diffs) || !CollectionUtils.isEmpty(exceptions);
    }

    void logSuccess() {
        log.info("");
        log.info("************** SUCCESS ***************");
        log.info("");
    }
}
