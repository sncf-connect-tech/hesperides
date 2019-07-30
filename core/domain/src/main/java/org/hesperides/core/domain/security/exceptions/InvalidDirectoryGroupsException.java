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
package org.hesperides.core.domain.security.exceptions;

import org.springframework.util.CollectionUtils;

import java.util.List;

public class InvalidDirectoryGroupsException extends IllegalArgumentException {

    public InvalidDirectoryGroupsException(List<String> unresolvedDirectoryGroupCNs, List<String> ambiguousDirectoryGroupCNs) {
        super(buildMessage(unresolvedDirectoryGroupCNs, ambiguousDirectoryGroupCNs));
    }

    public InvalidDirectoryGroupsException(String message) {
        super(message);
    }

    private static String buildMessage(List<String> unresolvedDirectoryGroupCNs, List<String> ambiguousDirectoryGroupCNs) {
        String message = "";
        if (!CollectionUtils.isEmpty(unresolvedDirectoryGroupCNs)) {
            message += "Unresolved group CNs: " + String.join(" - ", unresolvedDirectoryGroupCNs);
        }
        if (!CollectionUtils.isEmpty(ambiguousDirectoryGroupCNs)) {
            message += "Ambiguous group CNs: " + String.join(" - ", ambiguousDirectoryGroupCNs);
        }
        return message;
    }
}
